package com.dcy.rpc.task;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.constant.MessageConstant;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Kyle
 * @date 2024/04/08
 * <p>
 * sending heart beat to the service address (provider)
 */
@Slf4j
public class SendHeartbeatRequest implements Runnable{

    private String serviceName;
    private InetSocketAddress address;
    private GlobalConfig globalConfig;

    public SendHeartbeatRequest(String serviceName, InetSocketAddress address, GlobalConfig globalConfig) {
        this.serviceName = serviceName;
        this.address = address;
        this.globalConfig = globalConfig;
    }


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

        try {
            // send heartbeat request
            channel.writeAndFlush(requestProtocol);
            ConsumerCache.FUTURES_NAP.put(requestId, completableFuture);
            // get heartbeat result
            Object res = completableFuture.get(5, TimeUnit.SECONDS);
            if (!res.toString().equals(MessageConstant.HEARTBEAT_REQUEST)) {
                removeInvalidAddress();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            removeInvalidAddress();
            log.error("Heartbeat detection failed, address is -> {}", address);
        } finally {
            // remove completableFuture
            ConsumerCache.FUTURES_NAP.remove(requestId);
        }
    }

    private void removeInvalidAddress() {
        //NettyCache.CHANNEL_MAP.remove(address);
        boolean remove = ConsumerCache.SERVICE_ADDRESS_MAP.get(serviceName).remove(address);
        log.debug("removeInvalidAddress remove -> {}, service -> {} address -> {}", remove, serviceName, address);
        if (remove) {
            List<InetSocketAddress> addressList = NettyCache.PENDING_REMOVE_ADDRESS_MAP.computeIfAbsent(serviceName, k -> new ArrayList<>());
            addressList.add(address);
            // re-loadBalance
            reloadBalance(serviceName);
        }
    }

    private void reloadBalance(String serviceName) {
        Loadbalancer loadbalancer = ConsumerCache.LOADBALANCER_MAP.get(serviceName);
        loadbalancer.reloadBalance(ConsumerCache.SERVICE_ADDRESS_MAP.get(serviceName));
        log.debug("The serviceName {} has been successfully reload balanced...", serviceName);
    }
}
