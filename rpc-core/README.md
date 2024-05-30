# 1.Development Step

1.init repo

2.Initialize both base launchers

3.Write Netty communication between two parties

- provider server
- consumer server
- Simple communication test between two parties

4.Both sides scan the specified classpath

- The provider scans classes marked with the specified annotation and creates the corresponding service reference configuration
- The consumer scans classes marked with specified annotations and creates corresponding proxy objects  (In non-spring environment, unsuccessful)
  - Change: manually obtain the proxy object

5.Communication between two parties

- Consumer sends msg
- Provider receive msg
- Provider call the method by reflect
- Provider send result
- Consumer receive msg

6.Write serializer

- JDK

7.Write compressor

- deflate

8.Unpacking and pasting data

- writing protocol
- modify consumer Out Bound Handler
- modify consumer In Bound Handler
- modify provider Out Bound Handler
- modify consumer In Bound Handler

9.Implement registration center - zookeeper

- Code of which provider to publish services to the registry
- Code of which consumer can lookup a address from registry

10. Load balancer

- create strategy method for load balancer
- create load balancer
  - round robin

11.Heartbeat detection (Automatic detection using netty)

- Consumer send
- Provider receive

12.Dynamic node online and offline

- Using the mechanism of zkp

13.Create Redis registry center

- add address node to redis
- address node need to delete when program stop
