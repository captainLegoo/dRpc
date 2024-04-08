package com.dcy.rpc.task;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.constant.MessageConstant;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

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

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 5, 8, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(4), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void run() {
        log.debug("Heartbeat Detection TIme -> {}", new Date());

        for (Map.Entry<String, List<InetSocketAddress>> entry : ConsumerCache.SERVICE_ADDRESS_MAP.entrySet()) {
            String serviceName = entry.getKey();
            log.debug("Start detect serviceName is -> {}", serviceName);
            List<InetSocketAddress> inetSocketAddressList = entry.getValue();
            // list convert to list
            InetSocketAddress[] inetSocketAddressArray = inetSocketAddressList.toArray(new InetSocketAddress[0]);
            for (InetSocketAddress address : inetSocketAddressArray) {
                // send request
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        // encapsulate the message
                        long requestId = globalConfig.getIdGenerator().getId();
                        RequestProtocol requestProtocol = new RequestProtocol()
                                .setRequestId(requestId)
                                .setRequestType(RequestTypeEnum.HEART.getId())
                                .setCompressType(globalConfig.getCompressType().getCompressId())
                                .setSerializeType(globalConfig.getSerializableType().getSerializeId())
                                .setTimeStamp(new Date().getTime())
                                .setRequestPayload(null);

                        // get available channel
                        Channel channel = ConsumerNettyStarter.getNettyChannel(address);
                        // create CompletableFuture and add to cache, Waiting to receive return information
                        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                        // send heartbeat request
                        channel.writeAndFlush(requestProtocol);
                        ConsumerCache.FUTURES_NAP.put(requestId, completableFuture);
                        // get heartbeat result
                        try {
                            String res = (String) completableFuture.get(5, TimeUnit.SECONDS);
                            if (!res.equals(MessageConstant.HEARTBEAT_REQUEST) || Objects.isNull(res)) {
                                inetSocketAddressList.remove(address);
                                log.error("Heartbeat detection failed, serviceName is -> {}, address is -> {}", serviceName, address);
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            inetSocketAddressList.remove(address);
                            log.error("Heartbeat detection failed, serviceName is -> {}, address is -> {}", serviceName, address);
                            //throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }
}
