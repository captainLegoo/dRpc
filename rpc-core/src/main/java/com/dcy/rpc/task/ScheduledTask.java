package com.dcy.rpc.task;

import com.dcy.listen.ListenRedisServiceAddress;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.listen.ListenZkpServiceAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author Kyle
 * @date 2024/04/11
 * Scheduled task
 * - 1.Sending heartbeat detection
 * - 2.Using curator mechanism to monitor address
 */
@Slf4j
public class ScheduledTask {

    private final GlobalConfig globalConfig;
    private final ScheduledExecutorService scheduledExecutorService = ScheduledThreadPool.getScheduledExecutorService();

    public ScheduledTask(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public void startDoingTask() {

        scheduleFixedDelayTask(scheduledExecutorService, new CheckPendingOnlineAddress(), 5, 3);
        scheduleFixedDelayTask(scheduledExecutorService, new CheckPendingOfflineAddressTask(), 5, 3);

        // listen address
        if (globalConfig.getRegistryConfig().getRegistryCenterEnum().equals(RegistryCenterEnum.ZOOKEEPER)) {
            log.info("【ZooKeeper】Start monitoring service address changes....");
            String host = globalConfig.getRegistryConfig().getHost();
            int port = globalConfig.getRegistryConfig().getPort();
            String clientAddress = host + ":" +port;
            scheduledExecutorService.submit(new ListenZkpServiceAddress(
                    clientAddress,
                        NettyCache.PENDING_REMOVE_ADDRESS_MAP,
                        NettyCache.PENDING_ADD_ADDRESS_MAP
            ));
        } else if (globalConfig.getRegistryConfig().getRegistryCenterEnum().equals(RegistryCenterEnum.REDIS)) {
            log.info("【Redis】Start monitoring service address changes....");
            scheduleFixedDelayTask(scheduledExecutorService, new ListenRedisServiceAddress(
                    ConsumerCache.SERVICE_ADDRESS_MAP,
                    NettyCache.PENDING_REMOVE_ADDRESS_MAP,
                    NettyCache.PENDING_ADD_ADDRESS_MAP
            ), 5, 5);
        }
    }

    /**
     * schedule fixed delay task
     * @param scheduler
     * @param task
     * @param initialDelay
     * @param delay
     */
    private void scheduleFixedDelayTask(ScheduledExecutorService scheduler, Runnable task, long initialDelay, long delay) {
        scheduler.scheduleWithFixedDelay(task, initialDelay, delay, TimeUnit.SECONDS);
    }
}
