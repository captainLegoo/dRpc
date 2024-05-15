# HandsOn-RPC-framework

HandsOn-RPC-framework
![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/8733bf41593148aca1548a491daa94d0.png)



# Structure

```shell
HandsOn-RPC-framework
 |- rpc-common
 |- rpc-compress
 	|- rpc-compress-deflate
 |- rpc-core
 |- rpc-interface
 |- rpc-registrycenter
 	|- rpc-registrycenter-zookeeper
 |- rpc-serialization
 	|- rpc-serialization-jdk
 |- rpc-test
 	|- rpc-test-api
 	|- rpc-test-consumer
 	|- rpc-test-provider
```



# Zookeeper Node

```
rpc-metadata
 L providers
 	L service (Interface name)
 		L node1 [data]	(Node name：ip:port) (Data: related feature data/configuration, etc.)
 		L node2 [data]
 		L node3 [data]
 L consumers
 	L service
 		L node1 [data]
 		L node2 [data]
 		L node3 [data]
 L config
.........
```





# **Non-Spring project configuration**

## **Consumer**

- name (optional)
- registry - middleware / Ip address / port
- serializer
- compressor
- load balancer
- reference (Enable online and offline detection of dynamic nodes)

```java
DRpcBootstrap.getInstance()
    .setBootstrapName("RPC-consumer")
    .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.64.128", 2181)
    .serialize(SerializeTypeEnum.JDK)
    .compress(CompressTypeEnum.DEFLATE)
    .loadbalancer(LoadbalancerTypeEnum.ROUND_ROBIN)
    .reference();

BookService bookService = new BookServiceImpl();
bookService.writeReaderName();
```



Get the proxy object for variables that need to be injected remotely `new ProxyConfig<>(UserService.class).get()`

```java
public class BookServiceImpl implements BookService {

    private UserService userService = new ProxyConfig<>(UserService.class).get();

    @Override
    public void writeReaderName() {
        String string = userService.sayHello("dcy");
        System.out.println("writeReaderName = " + string);
    }
}
```



## **Provider**

- name (optional)
- port
- In which path should the package be published
- starter

```java
DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-Provider")
                .port(9000)
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.30.74", 2181)
                .scanAndPublish("com.dcy.rpc.service.impl")
                .start();
```



Add `@RpcService` to the class to be published

```java
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public String sayHello(String stuName) {
        return "hello " + stuName + ". How are you?";
    }
}
```



# Spring project configuration

## **Consumer**

- Configure the default startup as a bean
- Implements `BeanPostProcessor`
  - Override `postProcessAfterInitialization`

```java
@Configuration
@AutoConfigureAfter
public class RpcConfig implements BeanPostProcessor {

    @Bean
    public DRpcBootstrap rpcConsumerConfig() {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-consumer")
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.64.128", 2181)
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
```



Consumer

- Create a proxy object for consumer service that needing `new ProxyConfig<>(UserService.class).get();`

- Use `@RpcReference`

```java
@RestController
@RequestMapping("/payment")
public class PaymentController {

    //private final UserService userService = new ProxyConfig<>(UserService.class).get();
    @RpcReference
    private UserService userService;

    @GetMapping("/{accountName}")
    public String payment(@PathVariable String accountName) {
        return userService.sayHello(accountName);
    }
}
```





## **Provider**

Configure the default startup as a bean

```java
@Configuration
public class RpcConfig {
    @Bean
    public DRpcBootstrap rpcProviderConfig() {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-Provider")
                .port(9600)
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.64.128", 2181)
                .scanAndPublish("com.dcy.service.impl")
                .start();
        return DRpcBootstrap.getInstance();
    }
}
```



Use `@RpcService` to publish services to the registration center

```java
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public String sayHello(String name) {
        return "Rpc-Provider say Hello to: " + name;
    }
}

```

