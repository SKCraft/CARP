package com.skcraft.cardinal.service.remotecommand;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.skcraft.cardinal.api.ServiceError;
import com.skcraft.cardinal.api.WebService;
import com.skcraft.cardinal.event.ReloadEvent;
import com.skcraft.cardinal.service.hive.Hive;
import com.skcraft.cardinal.service.hive.permission.Context;
import com.skcraft.cardinal.service.hive.permission.Subject;
import com.skcraft.cardinal.profile.ProfileId;
import com.skcraft.cardinal.util.RetryMutex;
import com.skcraft.cardinal.util.event.EventBus;
import com.skcraft.cardinal.util.event.Subscribe;
import lombok.extern.java.Log;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class DefaultRemoteCommandManager implements RemoteCommandManager {
    private static final Pattern COMMAND_RE = Pattern.compile("^([^ \\t]+).*");
    private final RetryMutex loadRetry = new RetryMutex(this::fetchData);
    private ConcurrentMap<String, CommandSpec> commands = new ConcurrentHashMap<>();
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private final WebService service;
    private final Hive hive;
    private final String serverId;

    @Inject
    public DefaultRemoteCommandManager(WebService service, Hive hive, EventBus eventBus, @Named("service.id") String serverId) {
        this.service = service;
        this.hive = hive;
        this.serverId = serverId;
        loadRetry.start();
        eventBus.register(this);
    }

    @Subscribe
    public void onReload(ReloadEvent event) {
        log.info("Reloading all remote commands...");
        loadRetry.start();
    }

    private boolean fetchData() throws IOException, ServiceError {
        try {
            CommandData data = service.get("/services/" + serverId + "/commands", CommandData.class);
            synchronized (this) {
                setCommands(data.getCommands());
                log.info(String.format("Loaded %d command(s)", commands.size()));
            }
            return true;
        } catch (IOException | ServiceError e) {
            log.log(Level.WARNING, "Failed to fetch command data", e);
            throw e;
        }
    }

    private void setCommands(Collection<CommandSpec> commands) {
        ConcurrentMap<String, CommandSpec> map = new ConcurrentHashMap<>();
        for (CommandSpec spec : commands) {
            map.put(spec.getName().toLowerCase(), spec);
            for (String alias : spec.getAliases()) {
                map.put(alias.toLowerCase(), spec);
            }
        }
        this.commands = map;
    }

    @Override
    @Nullable
    public ListenableFuture<Response> execute(ProfileId sender, String message, Context context) {
        Matcher m = COMMAND_RE.matcher(message);
        if (m.matches()) {
            String command = m.group(1).toLowerCase();
            CommandSpec spec = commands.get(command);
            if (spec != null) {
                return executorService.submit(() -> {
                    if (!spec.getRequire().contains("*")) {
                        Subject subject = hive.getSubject(sender);
                        boolean authorized = false;
                        for (String permission : spec.getRequire()) {
                            if (subject.hasPermission(permission, context)) {
                                authorized = true;
                            } else {
                                throw new CommandException("You don't have permission to use this command.");
                            }
                        }
                        if (!authorized) {
                            throw new CommandException("You don't have permission to use this command.");
                        }
                    }

                    Request request = new Request(sender, message, new HashMap<>());
                    try {
                        return service.request("POST", "/services/" + serverId + "/commands/execute", Response.class, request);
                    } catch (IOException e) {
                        throw new CommandException("Failed to execute your command: could not contact the server. Please try again later.");
                    } catch (ServiceError serviceError) {
                        throw new CommandException(serviceError.getErrors().get(0));
                    }
                });
            }
        }
        return null;
    }
}
