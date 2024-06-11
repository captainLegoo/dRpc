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
    //private final ThreadPoolExecutor pool;

    public ScheduledTask(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        //this.pool = new ThreadPoolExecutor(
        //        10, // Number of core threads
        //        10, // Maximum number of threads
        //        0L, // The maximum time an idle thread waits for a new task
        //        TimeUnit.MILLISECONDS,
        //        new LinkedBlockingQueue<>(), // task queue
        //        Executors.defaultThreadFactory(),
        //        new ThreadPoolExecutor.AbortPolicy() // Rejection strategy after both queue and thread pool are full
        //);
    }

    public void startDoingTask() {
        //scheduler.scheduleWithFixedDelay(new HeartbeatDetectionTask(),
        //        15,
        //        5,
        //        TimeUnit.SECONDS);

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

    private class HeartbeatDetectionTask implements Runnable {

        private final GlobalConfig globalConfig = DRpcBootstrap.getInstance().getGlobalConfig();

        @Override
        public void run() {
            log.debug("Heartbeat Detection Time -> 【{}】...", new Date());

            try {
                //checkPendingOnlineAddress();
                //heartbeatDetectionTask();
            } catch (Exception e) {
                log.error("HeartbeatDetectionTask error: {}", e.getMessage(), e);
            }
        }

        private void checkPendingOnlineAddress() {
            NettyCache.PENDING_ADD_ADDRESS_MAP.forEach((serviceName, inetSocketAddressList) -> {
                List<InetSocketAddress> addressList = ConsumerCache.SERVICE_ADDRESS_MAP.computeIfAbsent(serviceName, k -> new ArrayList<>());

                // Iterate over each address in PENDING_ADD_ADDRESS_MAP and process them
                Iterator<InetSocketAddress> iterator = inetSocketAddressList.iterator();
                while (iterator.hasNext()) {
                    InetSocketAddress address = iterator.next();
                    Channel channel = ConsumerNettyStarter.getNettyChannel(address);
                    log.info("HeartbeatDetectionTask checking channel -> {}", channel);

                    // If the address is not in addressList, add it and reload the load balancer if it exists
                    if (!addressList.contains(address)) {
                        addressList.add(address);
                        Loadbalancer loadbalancer = ConsumerCache.LOADBALANCER_MAP.get(serviceName);
                        if (loadbalancer != null) {
                            loadbalancer.reloadBalance(addressList);
                        }
                        log.debug("HeartbeatDetectionTask reloadBalance addressList -> {}", addressList);

                        // Remove processed addresses from PENDING_ADD_ADDRESS_MAP
                        iterator.remove();
                    }
                }

                // If the list in PENDING_ADD_ADDRESS_MAP is empty, delete the entire entry
                if (inetSocketAddressList.isEmpty()) {
                    NettyCache.PENDING_ADD_ADDRESS_MAP.remove(serviceName);
                }
            });
        }

        private void heartbeatDetectionTask() {
            if (ConsumerCache.SERVICE_ADDRESS_MAP.isEmpty()) return;
            ConsumerCache.SERVICE_ADDRESS_MAP.forEach((serviceName, inetSocketAddressList) -> {
                log.debug("Start detect serviceName is -> 【{}】...", serviceName);
                log.debug("Start detect inetSocketAddressList is -> 【{}】...", inetSocketAddressList);
                for (InetSocketAddress address : inetSocketAddressList) {
                    // Submit tasks to the thread pool for execution
                    GlobalThreadPool.pool.execute(new SendHeartbeatRequest(serviceName, address, globalConfig));
                }
            });
        }
    }

}
