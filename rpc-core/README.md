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
