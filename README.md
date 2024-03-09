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

**Consumer**

- name (optional)
- serializer
- compressor

```java
DRpcBootstrap.getInstance()
    .setBootstrapName("RPC-consumer")
    .registry(null)
    .serialize(SerializeTypeEnum.JDK)
    .compress(CompressTypeEnum.DEFLATE);

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





**Provider**

- name (optional)
- port
- In which path should the package be published
- starter

```java
DRpcBootstrap.getInstance()
    .setBootstrapName("RPC-Provider")
    .port(9000)
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

