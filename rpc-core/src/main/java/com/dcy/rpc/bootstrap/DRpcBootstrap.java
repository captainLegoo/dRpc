package com.dcy.rpc.bootstrap;

import com.dcy.rpc.annotation.RpcService;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.config.RegistryConfig;
import com.dcy.rpc.config.ServiceConfig;
import com.dcy.rpc.netty.ProviderNettyStarter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
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
     *
     * @param registryConfig
     * @return
     */
    public DRpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * --------------------------------Related APIs for service callers--------------------------------
     */

    public DRpcBootstrap serialize() {
        return this;
    }

    public DRpcBootstrap compress() {
        return this;
    }

    public DRpcBootstrap reference() {
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
    //public DRpcBootstrap scanAndPublish(String packageName) {
    //    // 1.Get the full class names of all classes under it through the package name
    //    List<String> classNameList = getAllClassNames(packageName);
    //
    //    // 2.Obtain the specific Class through reflection
    //    List<? extends Class<?>> classList = classNameList
    //            .stream()
    //            .map(className -> {
    //                try {
    //                    return Class.forName(className);
    //                } catch (ClassNotFoundException e) {
    //                    throw new RuntimeException(e);
    //                }
    //            })
    //            .filter(clazz -> clazz.getAnnotation(RpcService.class) != null)
    //            .collect(Collectors.toList());
    //
    //    // 3.Batch encapsulation into ServiceConfig
    //    for (Class<?> clazz : classList) {
    //        Class<?>[] interfaces = clazz.getInterfaces();
    //        Object instance;
    //        try {
    //            instance = clazz.getDeclaredConstructor().newInstance();
    //        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
    //                 NoSuchMethodException e) {
    //            throw new RuntimeException(e);
    //        }
    //
    //        // 3.1.May be multiple interfaces corresponding to the same implementation class
    //        for (Class<?> anInterface : interfaces) {
    //            ServiceConfig<?> serviceConfig = new ServiceConfig<>();
    //            serviceConfig.setInterfaceRef(anInterface);
    //            serviceConfig.setImplRef(instance);
    //            if (log.isDebugEnabled()) {
    //                log.debug("Scanned to service {}, ready to publish", anInterface);
    //            }
    //
    //            // 4.invoke publish method
    //            boolean isPublish = publish(serviceConfig);
    //            if (log.isDebugEnabled()) {
    //                log.debug("The service【{}】, {}", anInterface.getName(), isPublish ? "Published successfully" : "Publishing failed");
    //            }
    //        }
    //    }
    //    return this;
    //}

    /**
     * publish service to registration center
     * @param serviceConfig
     * @return
     */
    //private boolean publish(ServiceConfig serviceConfig) {
    //    // 1.Registering services as nodes in the registry
    //    boolean isRegister = globalConfig.getRegistryCenter().registerService(serviceConfig, globalConfig.getPort());
    //
    //    // 2.If registration is successful, the service is cached locally
    //    if (isRegister) {
    //        ProviderCache.SERVERS_LIST.put(serviceConfig.getInterfaceRef().getName(), serviceConfig);
    //        return true;
    //    }
    //    return false;
    //}

    /**
     * service provider start
     */
    public void start() {
        ProviderNettyStarter providerNettyStarter = new ProviderNettyStarter(globalConfig.getPort());
        providerNettyStarter.start();
    }

    /**
     * Get the full class names of all classes in this package
     *
     * @param packageName
     * @return
     */
    private List<String> getAllClassNames(String packageName) {
        // 1.Get the absolute path by passing in packageName
        // com.dcy.xxx.yyy -> D://xxx/xww/sss/com/dcy/xxx/yyyl
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);

        if (url == null) {
            throw new RuntimeException("Path does not exist during package scan");
        }

        String absolutePath = url.getPath();

        List<String> classNameList = new ArrayList<>();
        classNameList = recursionFile(absolutePath, classNameList, basePath);

        return classNameList;
    }

    /**
     * Process files recursively
     *
     * @param absolutePath
     * @param classNameList
     * @param basePath
     * @return
     */
    private List<String> recursionFile(String absolutePath, List<String> classNameList, String basePath) {
        // 1.Get file
        File file = new File(absolutePath);
        // 2.Determine whether the file is a folder
        if (file.isDirectory()) {
            // Find all files in a folder
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNameList;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    // 递归调用
                    recursionFile(child.getAbsolutePath(), classNameList, basePath);
                } else {
                    // 文件 --> 类的全类名称
                    String className = getCLassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNameList.add(className);
                }
            }

        } else {
            // file --> Full class name of the class
            String className = getCLassNameByAbsolutePath(absolutePath, basePath);
            classNameList.add(className);
        }
        return classNameList;
    }

    /**
     * Get the full class name of a class by absolute path
     *
     * @param absolutePath
     * @return
     */
    private String getCLassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\"))).replaceAll("\\\\", ".");
        String substring = fileName.substring(0, fileName.indexOf(".class"));
        return substring;
    }
}
