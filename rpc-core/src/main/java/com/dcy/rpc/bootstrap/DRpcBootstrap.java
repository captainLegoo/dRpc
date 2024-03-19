package com.dcy.rpc.bootstrap;

import com.dcy.rpc.annotation.RpcReference;
import com.dcy.rpc.annotation.RpcService;
import com.dcy.rpc.cache.ProviderCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.config.RegistryConfig;
import com.dcy.rpc.config.ServiceConfig;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.LoadbalancerTypeEnum;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.factory.RegistryFactory;
import com.dcy.rpc.netty.ProviderNettyStarter;
import com.dcy.rpc.proxy.ProxyConfig;
import com.dcy.rpc.registry.Registry;
import com.dcy.rpc.util.GetHostAddress;
import com.dcy.rpc.util.ScanPackage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kyle
 * @date 2024/02/19
 * <p>
 * Provider Starter
 * - singleton: lazy, double-check locking, prevent reflection intrusion
 * provider config:
 * - registry center
 * - port
 * - scan and publish packages/classes
 * - load balance
 * consumer config:
 * - registry center
 * - serialize
 * - compress
 * - reference
 */
@Slf4j
public class DRpcBootstrap {
    private static volatile DRpcBootstrap instance;
    private static final GlobalConfig globalConfig = new GlobalConfig();
    private static Registry registry;

    private DRpcBootstrap() {
        if (instance != null) {
            throw new RuntimeException("ProviderStarter is a singleton");
        }
    }

    /**
     * create class instance
     *
     * @return this
     */
    public static DRpcBootstrap getInstance() {
        if (instance == null) {
            synchronized (DRpcBootstrap.class) {
                if (instance == null) {
                    instance = new DRpcBootstrap();
                }
            }
        }
        return instance;
    }

    /**
     * set bootstrap name
     *
     * @param bootstrapName
     * @return this
     */
    public DRpcBootstrap setBootstrapName(@NonNull String bootstrapName) {
        globalConfig.setBootstrapName(bootstrapName);
        return this;
    }

    /**
     * registry
     * @param registryCenterEnum
     * @param host
     * @param port
     * @return
     */
    public DRpcBootstrap registry(RegistryCenterEnum registryCenterEnum, String host, int port) {
        RegistryConfig registryConfig = new RegistryConfig(registryCenterEnum, host, port);
        globalConfig.setRegistryConfig(registryConfig);
        // connect to registry
        registry = RegistryFactory.getRegistry(getGlobalConfig().getRegistryConfig());
        if (registry != null) {
            globalConfig.setRegistry(registry);
        }
        return this;
    }

    /**
     * --------------------------------Related APIs for service callers--------------------------------
     */

    public DRpcBootstrap serialize(SerializeTypeEnum serializeTypeEnum) {
        globalConfig.setSerializableType(serializeTypeEnum);
        return this;
    }

    public DRpcBootstrap compress(CompressTypeEnum compressTypeEnum) {
        globalConfig.setCompressType(compressTypeEnum);
        return this;
    }

    public DRpcBootstrap loadbalancer(LoadbalancerTypeEnum loadbalancerTypeEnum) {
        globalConfig.setLoadbalancerTypeEnum(loadbalancerTypeEnum);
        return this;
    }

    /**
     * Create a proxy object based on the package path and specific variables
     * TODO: In non-Spring environments, modifying properties through reflection will fail.
     * @param packageName
     * @return
     */
    public DRpcBootstrap reference(String packageName) {
        try {
            List<String> classNameList = ScanPackage.scanPackage(packageName);
            List<? extends Class<?>> classList = classNameList.stream().map(className -> {
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

            for (Class<?> clazz : classList) {
                Object instance = clazz.getDeclaredConstructor().newInstance();

                Field[] declaredFieldsArray = clazz.getDeclaredFields();
                List<Field> fieldList = Arrays.stream(declaredFieldsArray).filter(field -> {
                    return field.getAnnotation(RpcReference.class) != null;
                }).collect(Collectors.toList());

                // create each field that reference
                for (Field field : fieldList) {
                    ProxyConfig<?> proxyConfig = new ProxyConfig<>(field.getType());
                    // Use ProxyConfig to create a proxy object and inject the proxy object into the field
                    try {
                        field.setAccessible(true);
                        field.set(instance, proxyConfig.get());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to set proxy instance to field: " + field.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * --------------------------------Related APIs of service providers--------------------------------
     */

    public DRpcBootstrap port(int port) {
        globalConfig.setPort(port);
        return this;
    }

    /**
     * scan package
     *
     * @param packageName
     * @return
     */
    public DRpcBootstrap scanAndPublish(String packageName) {
        // 1.Get the full class names of all classes under it through the package name
        List<String> classNameList = ScanPackage.scanPackage(packageName);

        // 2.Obtain the specific Class through reflection
        List<? extends Class<?>> classList = classNameList
                .stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(clazz -> clazz.getAnnotation(RpcService.class) != null)
                .collect(Collectors.toList());

        // 3.Batch encapsulation into ServiceConfig
        for (Class<?> clazz : classList) {
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance;
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // 3.1.May be multiple interfaces corresponding to the same implementation class
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterfaceRef(anInterface);
                serviceConfig.setImpl(instance);
                log.info("Scanned to service 【{}】, ready to publish", anInterface);

                // 4.invoke publish method
                boolean isPublish = publish(serviceConfig);
                log.info("The service【{}】, {}", anInterface, isPublish ? "Published successfully" : "Publishing failed");
            }
        }
        return this;
    }

    /**
     * publish service to registration center
     * @param serviceConfig
     * @return
     */
    private boolean publish(ServiceConfig<?> serviceConfig) {
        // 1.Registering services as nodes in the registry
        //boolean isRegister = globalConfig.getRegistryCenter().registerService(serviceConfig, globalConfig.getPort());
        String localIPAddress = GetHostAddress.getLocalIPAddress();
        log.debug("localIPAddress: {}", localIPAddress);
        boolean isPublish = registry.register(serviceConfig.getInterfaceRef().getName(), localIPAddress, globalConfig.getPort());

        // 2.If registration is successful, the service is cached locally
        if (isPublish) {
            ProviderCache.SERVERS_LIST.put(serviceConfig.getInterfaceRef().getName(), serviceConfig);
            return true;
        }
        return false;
    }

    /**
     * service provider start
     */
    public void start() {
        ProviderNettyStarter providerNettyStarter = new ProviderNettyStarter(globalConfig.getPort());
        providerNettyStarter.start();
    }

    /**
     * --------------------------------Get the object of this method--------------------------------
     */

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }
}
