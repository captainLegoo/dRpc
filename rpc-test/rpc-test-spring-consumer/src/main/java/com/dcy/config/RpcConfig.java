package com.dcy.config;

import com.dcy.rpc.annotation.RpcReference;
import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.LoadbalancerTypeEnum;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.proxy.ProxyConfig;
import com.dcy.rpc.util.ScanPackage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kyle
 * @date 2024/03/30
 * <p>
 * rpc-consumer config
 */
@Configuration
@AutoConfigureAfter
public class RpcConfig implements BeanPostProcessor {

    @Bean
    public DRpcBootstrap rpcConsumerConfig() {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-consumer")
                //.registry(RegistryCenterEnum.ZOOKEEPER, "192.168.205.132", 2181)
                .registry(RegistryCenterEnum.REDIS, "192.168.205.128", 6379)
                .serialize(SerializeTypeEnum.JDK)
                .compress(CompressTypeEnum.DEFLATE)
                .loadbalancer(LoadbalancerTypeEnum.ROUND_ROBIN)
                .reference();

        return DRpcBootstrap.getInstance();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        try {
                Field[] declaredFieldsArray = bean.getClass().getDeclaredFields();
                List<Field> fieldList = Arrays
                        .stream(declaredFieldsArray)
                        .filter(field -> {
                    return field.getAnnotation(RpcReference.class) != null;
                }).collect(Collectors.toList());

                // create each field that reference
                for (Field field : fieldList) {
                    ProxyConfig<?> proxyConfig = new ProxyConfig<>(field.getType());
                    // Use ProxyConfig to create a proxy object and inject the proxy object into the field
                    try {
                        field.setAccessible(true);
                        field.set(bean, proxyConfig.get());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to set proxy instance to field: " + field.getName(), e);
                    }
                }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process @RpcReference annotation", e);
        }
        return bean;
    }
}
