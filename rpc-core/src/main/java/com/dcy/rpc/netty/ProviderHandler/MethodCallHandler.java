package com.dcy.rpc.netty.ProviderHandler;

import com.dcy.rpc.cache.ProviderCache;
import com.dcy.rpc.config.ServiceConfig;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.entity.ResponseProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Objects;

/**
 * @author Kyle
 * @date 2024/02/29
 * <p>
 * The provider calls methods through reflection
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RequestProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestProtocol requestProtocol) throws Exception {
        log.debug("MethodCallHandler receive request，id is 【{}】", requestProtocol.getRequestId());

        RequestPayload requestPayload = requestProtocol.getRequestPayload();
        ServiceConfig<?> serviceConfig = ProviderCache.SERVERS_LIST.get(requestPayload.getInterfaceName());

        if (Objects.isNull(serviceConfig)) {
            log.error("Service Not Found");
            return;
        }

        Class<?> instance = serviceConfig.getImpl().getClass();
        Method method = instance.getMethod(requestPayload.getMethodName(), requestPayload.getParametersType());
        Object returnValue = method.invoke(serviceConfig.getImpl(), requestPayload.getParameterValue());
        ResponseProtocol responseProtocol = ResponseProtocol.builder()
                .requestId(requestProtocol.getRequestId())
                .code((byte) 1)
                .compressTypeId(requestProtocol.getCompressType())
                .serializeTypeId(requestProtocol.getSerializeType())
                .timeStamp(new Date().getTime())
                .responseBody(returnValue)
                .build();

        log.debug("Method call completed，id is 【{}】", requestProtocol.getRequestId());
        ctx.writeAndFlush(responseProtocol);
    }
}
