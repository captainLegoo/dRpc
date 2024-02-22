package com.dcy.rpc.example;


import org.apache.zookeeper.*;
import java.io.IOException;

public class ZooKeeperExample {
    private static final String CONNECT_STRING = "192.168.30.74:2181";
    private static final int SESSION_TIMEOUT = 3000;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, null);

        // 创建节点
        String path = "/exampleNode";
        zooKeeper.create(path, "exampleData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // 获取节点数据
        byte[] data = zooKeeper.getData(path, false, null);
        System.out.println("Node data: " + new String(data));

        zooKeeper.close();
    }
}
