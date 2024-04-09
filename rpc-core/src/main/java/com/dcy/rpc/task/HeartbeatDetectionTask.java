package com.dcy.rpc.task;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/04/08
 * <p>
 * Scheduled heartbeat detection task
 * Dynamic service address offline
 */
@Slf4j
public class HeartbeatDetectionTask implements Runnable{

    private final GlobalConfig globalConfig = DRpcBootstrap.getInstance().getGlobalConfig();

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 5, 8, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(4), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void run() {
        log.debug("Heartbeat Detection Time -> {}", new Date());

        for (Map.Entry<String, List<InetSocketAddress>> entry : ConsumerCache.SERVICE_ADDRESS_MAP.entrySet()) {
            String serviceName = entry.getKey();
            log.debug("Start detect serviceName is -> {}", serviceName);
            List<InetSocketAddress> inetSocketAddressList = entry.getValue();
            log.debug("Start detect inetSocketAddressList size() is -> {}", inetSocketAddressList.size());
            // list convert to list
            InetSocketAddress[] inetSocketAddressArray = inetSocketAddressList.toArray(new InetSocketAddress[0]);
            for (InetSocketAddress address : inetSocketAddressArray) {
                // send request
                pool.execute(new SendHeartbeatRequest(serviceName, address, globalConfig, inetSocketAddressList));
            }
        }
    }
}
