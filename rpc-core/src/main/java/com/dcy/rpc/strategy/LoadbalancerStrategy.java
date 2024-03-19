package com.dcy.rpc.strategy;

import com.dcy.rpc.enumeration.LoadbalancerTypeEnum;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.loadbalancer.impl.RoundLoadbalancer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2023/10/4 11:29
 * <p>
 * Loadbalancer Strategy method class
 */
@Slf4j
public class LoadbalancerStrategy {

    private final static Map<LoadbalancerTypeEnum, Loadbalancer> LOADBALANCER_MAP = new ConcurrentHashMap<>(8);


    static {
        LOADBALANCER_MAP.put(LoadbalancerTypeEnum.ROUND_ROBIN, new RoundLoadbalancer());
    }

    /**
     * Get Loadbalancer
     * @param loadbalancerTypeEnum
     * @return
     */
    public static Loadbalancer getLoadbalancer(LoadbalancerTypeEnum loadbalancerTypeEnum) {
        return LOADBALANCER_MAP.getOrDefault(loadbalancerTypeEnum, new RoundLoadbalancer());
    }
}
