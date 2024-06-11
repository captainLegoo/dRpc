package com.dcy.rpc.task;

import com.dcy.listen.ListenRedisServiceAddress;
import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.listen.ListenZkpServiceAddress;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
@Slf4j
public class ScheduledTask {

    private final GlobalConfig globalConfig;
    private final ScheduledExecutorService scheduler;

    public ScheduledTask(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void startDoingTask() {
        scheduler.scheduleWithFixedDelay(new checkPendingOnlineAddress(),
                12,
                3,
                TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(new CheckPendingOfflineAddressTask(),
                14,
                3,
                TimeUnit.SECONDS);

        // listen address
        if (globalConfig.getRegistryConfig().getRegistryCenterEnum().equals(RegistryCenterEnum.ZOOKEEPER)) {
            log.info("【ZooKeeper】Start monitoring service address changes....");
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
        } else if (globalConfig.getRegistryConfig().getRegistryCenterEnum().equals(RegistryCenterEnum.REDIS)) {
            log.info("【Redis】Start monitoring service address changes....");
            scheduler.scheduleWithFixedDelay(
                    new ListenRedisServiceAddress(
                            ConsumerCache.SERVICE_ADDRESS_MAP,
                            NettyCache.PENDING_REMOVE_ADDRESS_MAP,
                            NettyCache.PENDING_ADD_ADDRESS_MAP
                    ),
                    5,
                    5,
                    TimeUnit.SECONDS
            );
        }
    }

}
