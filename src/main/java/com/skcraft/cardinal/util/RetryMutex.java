package com.skcraft.cardinal.util;

import com.nurkiewicz.asyncretry.AsyncRetryExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.*;

public class RetryMutex {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AsyncRetryExecutor executor = new AsyncRetryExecutor(scheduler)
            .withExponentialBackoff(5000, 2)
            .withMaxDelay(1000 * 60 * 10)
            .withProportionalJitter()
            .retryInfinitely();
    private final Callable<?> callable;
    private final AtomicBoolean loading = new AtomicBoolean(false);

    public RetryMutex(Callable<?> callable) {
        checkNotNull(callable, "callable");
        this.callable = callable;
    }

    public void start() {
        if (loading.compareAndSet(false, true)) {
            executor.getWithRetry(callable).whenComplete((v, t) -> loading.set(false));
        }
    }
}
