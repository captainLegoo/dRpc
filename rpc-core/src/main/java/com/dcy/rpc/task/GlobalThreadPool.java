package com.dcy.rpc.task;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/05/15
 */
public class GlobalThreadPool {
    public static final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            10, // Number of core threads
            10, // Maximum number of threads
            0L, // The maximum time an idle thread waits for a new task
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), // task queue
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy() // Rejection strategy after both queue and thread pool are full
    );
}
