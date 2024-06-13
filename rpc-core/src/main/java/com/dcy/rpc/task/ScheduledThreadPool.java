package com.dcy.rpc.task;

import java.util.concurrent.*;

/**
 * @author Kyle
 * @date 2024/06/13
 * @description Unified scheduling task thread pool
 */
public class ScheduledThreadPool {
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            10,
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }
}
