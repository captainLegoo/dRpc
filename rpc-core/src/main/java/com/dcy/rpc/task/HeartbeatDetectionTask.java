package com.dcy.rpc.task;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/04/08
 * <p>
 * Scheduled task
 * - 1.Sending heartbeat detection
 * - 2.Using curator mechanism to monitor address
 */
@Slf4j
public class HeartbeatDetectionTask implements Runnable {

    private final GlobalConfig globalConfig = DRpcBootstrap.getInstance().getGlobalConfig();

    private static ThreadPoolExecutor pool;

    @Override
    public void run() {
        log.debug("Heartbeat Detection Time -> 【{}】...", new Date());

        int poolSize = 10;
        pool = new ThreadPoolExecutor(
                poolSize, // Number of core threads
                poolSize, // Maximum number of threads
                0L, // The maximum time an idle thread waits for a new task
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), // task queue
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() // Rejection strategy after both queue and thread pool are full
        );

        try {
            checkPendingOnlineAddress();
            heartbeatDetectionTask();
        } finally {
            // Make sure to close the thread pool after the task is completed
            pool.shutdown();
            try {
                // Wait for all tasks to complete or timeout
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // Cancel an ongoing task
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow(); // (Re-)Cancel if current thread also interrupted
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
        }
    }

    private void checkPendingOnlineAddress() {
        NettyCache.PENDING_ADD_ADDRESS_MAP.forEach((serviceName, inetSocketAddressList) -> {
            List<InetSocketAddress> addressList = ConsumerCache.SERVICE_ADDRESS_MAP.get(serviceName);
            if (addressList == null) {
                return;
            }
            inetSocketAddressList.removeIf(address -> {
                Channel channel = ConsumerNettyStarter.getNettyChannel(address);
                log.error("HeartbeatDetectionTask channel -> {}", channel);
                if (!addressList.contains(address)) {
                    addressList.add(address);
                    Loadbalancer loadbalancer = ConsumerCache.LOADBALANCER_MAP.get(serviceName);
                    if (loadbalancer != null) {
                        loadbalancer.reloadBalance(addressList);
                    }
                    log.info("HeartbeatDetectionTask reloadBalance addressList -> {}", addressList);
                    //log.info("Now LoadBalance addressList -> {}", ConsumerCache.LOADBALANCER_MAP.get(serviceName).getAddressList());
                    return true;
                }
                return false;
            });
            if (inetSocketAddressList.isEmpty()) {
                NettyCache.PENDING_ADD_ADDRESS_MAP.remove(serviceName);
            }
        });
    }



    private void heartbeatDetectionTask() {
        for (Map.Entry<String, List<InetSocketAddress>> entry : ConsumerCache.SERVICE_ADDRESS_MAP.entrySet()) {
            String serviceName = entry.getKey();
            log.debug("Start detect serviceName is -> 【{}】...", serviceName);
            List<InetSocketAddress> inetSocketAddressList = entry.getValue();
            log.debug("Start detect inetSocketAddressList is -> 【{}】...", inetSocketAddressList);
            //log.debug("Now loadbalancer service -> {} address -> {}", serviceName, ConsumerCache.LOADBALANCER_MAP.get(serviceName).getAddressList());
            for (InetSocketAddress address : inetSocketAddressList) {
                // Submit tasks to the thread pool for execution
                pool.execute(new SendHeartbeatRequest(serviceName, address, globalConfig));
            }
        }
    }
}
