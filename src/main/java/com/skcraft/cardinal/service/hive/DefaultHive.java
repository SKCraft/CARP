package com.skcraft.cardinal.service.hive;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.skcraft.cardinal.api.ServiceError;
import com.skcraft.cardinal.api.WebService;
import com.skcraft.cardinal.event.ReloadEvent;
import com.skcraft.cardinal.event.user.RefreshUsersEvent;
import com.skcraft.cardinal.profile.ConsoleUser;
import com.skcraft.cardinal.profile.ProfileId;
import com.skcraft.cardinal.service.hive.permission.Group;
import com.skcraft.cardinal.service.hive.permission.NullSubject;
import com.skcraft.cardinal.service.hive.permission.PermitSubject;
import com.skcraft.cardinal.service.hive.permission.Subject;
import com.skcraft.cardinal.service.hive.permission.UserSubject;
import com.skcraft.cardinal.util.DataAccessException;
import com.skcraft.cardinal.util.RetryMutex;
import com.skcraft.cardinal.util.event.EventBus;
import com.skcraft.cardinal.util.event.Subscribe;
import lombok.extern.java.Log;
import redis.clients.jedis.JedisPool;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

@Log
public class DefaultHive implements Hive {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final RetryMutex loadRetry = new RetryMutex(this::fetchData);
    private final WebService service;
    private final String serverId;
    private final JedisPool jedis;
    private ConcurrentMap<String, Group> groups = new ConcurrentHashMap<>();
    private Cache<ProfileId, User> userCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();
    private LoadingCache<User, UserSubject> subjectCache = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .weakKeys()
            .build(new CacheLoader<User, UserSubject>() {
                @Override
                public UserSubject load(User key) throws Exception {
                    return new UserSubject(mapGroups(key.getGroups()));
                }
            });

    @Inject
    public DefaultHive(WebService service, EventBus eventBus, JedisPool jedis, @Named("service.id") String serverId) {
        checkNotNull(service, "service");
        checkNotNull(serverId, "serverId");
        this.service = service;
        this.serverId = serverId;
        this.jedis = jedis;
        eventBus.register(this);
        loadRetry.start();
        scheduler.scheduleWithFixedDelay(() -> eventBus.post(new RefreshUsersEvent(this)), 2, 2, TimeUnit.MINUTES);
    }

    @Subscribe
    public void onReload(ReloadEvent event) {
        log.info("Reloading all user data...");
        loadRetry.start();
    }

    private boolean fetchData() throws IOException, ServiceError {
        try {
            HiveData data = service.get("/services/" + serverId + "/hive", HiveData.class);
            synchronized (this) {
                groups = data.getGroups();
                log.info(String.format("Loaded %d user group(s)", groups.size()));
                subjectCache.invalidateAll();
                // TODO: Reload users
            }
            return true;
        } catch (IOException | ServiceError e) {
            log.log(Level.WARNING, "Failed to fetch Hive data", e);
            throw e;
        }
    }

    private Set<Group> mapGroups(Collection<String> groupNames) {
        Set<Group> result = new HashSet<>();
        for (String name : groupNames) {
            Group group = groups.get(name);
            if (group != null) {
                result.add(group);
            } else {
                // TODO: Data is old?
            }
        }
        return result;
    }

    @Override
    public User login(ProfileId profileId) throws SessionRejectedException {
        ImmutableMap<String, Object> data = new ImmutableMap.Builder<String, Object>()
                .put("service", serverId)
                .put("profile", profileId)
                .build();

        try {
            LoginResponse response = service.request("POST", "/sessions", LoginResponse.class, data);
            if (response.isAccepted()) {
                User user = response.getUser();
                userCache.put(profileId, user);
                return user;
            } else {
                throw new SessionRejectedException(response.getRejectMessage());
            }
        } catch (IOException | ServiceError e) {
            throw new DataAccessException(e);
        }
    }

    @Nullable
    @Override
    public User get(ProfileId profileId) {
        return userCache.getIfPresent(profileId);
    }

    @Override
    public Subject getSubject(ProfileId profileId) {
        if (profileId instanceof ConsoleUser) {
            return new PermitSubject();
        }
        User user = get(profileId);
        if (user != null) {
            return subjectCache.getUnchecked(user);
        } else {
            return new NullSubject();
        }
    }

    @Override
    public void invite(String name, ProfileId referrer) throws ErrorResponse {
        ImmutableMap<String, Object> data = new ImmutableMap.Builder<String, Object>()
                .put("referrer", referrer)
                .build();

        try {
            service.request("POST", "/minecraft/whitelist/" + name, Map.class, data);
        } catch (ServiceError serviceError) {
            throw new ErrorResponse(serviceError.getErrors().get(0));
        } catch (IOException e) {
            throw new DataAccessException(e);
        }
    }
}
