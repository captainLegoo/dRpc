package com.dcy.rpc.task;

import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.listen.ListenZkpServiceAddress;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/04/11
 * Scheduled task
 * - 1.Sending heartbeat detection
 * - 2.Using curator mechanism to monitor address
 */
public class ScheduledTask {

    private final GlobalConfig globalConfig;

    public ScheduledTask(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }


    public void startDoingTask() {
        //
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new HeartbeatDetectionTask(),
                15,
                2,
                TimeUnit.SECONDS);

        // listen address
        String host = globalConfig.getRegistryConfig().getHost();
        int port = globalConfig.getRegistryConfig().getPort();
        String clientAddress = host + ":" +port;
        Thread thread = new Thread(
                new ListenZkpServiceAddress(
                        clientAddress,
                        NettyCache.PENDING_REMOVE_ADDRESS_MAP,
                        NettyCache.PENDING_ADD_ADDRESS_MAP
                )
        );
        thread.setDaemon(true);
        thread.start();
    }

}
