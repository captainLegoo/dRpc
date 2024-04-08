package com.dcy.rpc.task;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Kyle
 * @date 2024/04/08
 */
@Slf4j
public class SendHeartbeatRequest implements Runnable{

    private final String serviceName;
    private final InetSocketAddress address;
    private final GlobalConfig globalConfig;
    private final List<InetSocketAddress> inetSocketAddressList;

    public SendHeartbeatRequest(String serviceName, InetSocketAddress address, GlobalConfig globalConfig, List<InetSocketAddress> inetSocketAddressList) {
        this.serviceName = serviceName;
        this.address = address;
        this.globalConfig = globalConfig;
        this.inetSocketAddressList = inetSocketAddressList;
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
        if (channel == null) {
            removeInvalidAddress();
        }
        // create CompletableFuture and add to cache, Waiting to receive return information
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        try {
            // send heartbeat request
            channel.writeAndFlush(requestProtocol);
            ConsumerCache.FUTURES_NAP.put(requestId, completableFuture);
            // get heartbeat result
            Object res = completableFuture.get(5, TimeUnit.SECONDS);
            if (Objects.isNull(res) || !res.toString().equals(MessageConstant.HEARTBEAT_REQUEST)) {
                removeInvalidAddress();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            removeInvalidAddress();
            log.error("Heartbeat detection failed, address is -> {}", address);
            //throw new RuntimeException(e);
        }
    }

    private void removeInvalidAddress() {
        inetSocketAddressList.remove(address);
        NettyCache.CHANNEL_MAP.remove(address);
        ConsumerCache.SERVICE_ADDRESS_MAP.get(serviceName).remove(address);
    }
}
