package com.dcy.controller;

import com.dcy.common.result.Result;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.cache.ProxyCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/05/11
 */
@RestController
@RequestMapping("/checkdata")
@Api(tags = "Data")
public class GetDataController {

    @GetMapping("/loadbalancer")
    @ApiOperation("Loadbalancer Map")
    public Result<?> getLoadbalancerMap() {
        return Result.success(ConsumerCache.LOADBALANCER_MAP.toString());
    }

    @GetMapping("/serviceaddress")
    @ApiOperation("Service Address List")
    public Result<?> getServiceAddressMap() {
        return Result.success(ConsumerCache.SERVICE_ADDRESS_MAP);
    }

    @GetMapping("/channel")
    @ApiOperation("Channel Map")
    public Result<?> getChannelMap() {
        return Result.success(NettyCache.CHANNEL_MAP);
    }

    @GetMapping("/proxy/object")
    @ApiOperation("proxy object map")
    public Result<?> getProxyObjectMap() {
        return Result.success(ProxyCache.PROXY_OBJECT_CACHE_MAP.toString());
    }

    @GetMapping("/proxy/service")
    @ApiOperation("proxy service name")
    public Result<?> getProxyServiceNameSet() {
        return Result.success(ProxyCache.PROXY_NAME_CACHE_SET);
    }
}
