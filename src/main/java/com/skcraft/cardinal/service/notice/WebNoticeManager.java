package com.skcraft.cardinal.service.notice;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.skcraft.cardinal.api.ServiceError;
import com.skcraft.cardinal.api.WebService;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Log
public class WebNoticeManager implements NoticeManager {

    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private final WebService service;
    private final String serverId;

    @Inject
    public WebNoticeManager(WebService service, @Named("service.id") String serverId) {
        this.service = service;
        this.serverId = serverId;
    }

    @Override
    public ListenableFuture<Notice> getNext(String group) {
        return executorService.submit(new FetchTask(group));
    }

    private class FetchTask implements Callable<Notice> {
        private final String group;

        private FetchTask(String group) {
            this.group = group;
        }

        @Override
        public Notice call() throws Exception {
            try {
                return service.get("/services/" + serverId + "/notices/" + group + "/next/", Notice.class);
            } catch (IOException | ServiceError e) {
                log.log(Level.WARNING, "Failed to fetch notice", e);
                throw e;
            }
        }
    }

}
