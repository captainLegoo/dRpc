package com.dcy.rpc.registry;

import com.dcy.rpc.constant.ConnectConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author Kyle
 * @date 2024/03/09
 *
 * connect and publish service in zookeeper
 */
@Slf4j
public class ZookeeperRegistry implements Registry{
    private final CuratorFramework client;
    private String address;
    private int host;

    public ZookeeperRegistry(String address, int host) {
        this.address = address;
        this.host = host;
        this.client = connectZookeeper(address, host);
    }

    @Override
    public boolean register(String serviceName, String localIPAddress, int port) {
        String servicePath = ConnectConstant.NODE_DEFAULT_PATH + "/" + serviceName;
        String ipAddressPath = servicePath + "/" + localIPAddress + ":" + port;
        try {
            // create service path
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(servicePath);

            // create ip of service path
            String finalPath = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(ipAddressPath);

            return finalPath.equals(ipAddressPath);
        } catch (Exception e) {
            log.error("Failed to register service", e);
        }
        return false;
    }

    private CuratorFramework connectZookeeper(String address, int host) {

        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);

            CuratorFramework client2 = CuratorFrameworkFactory.builder()
                    .connectString(address + ":" + host)
                    .sessionTimeoutMs(60 * 1000)
                    .connectionTimeoutMs(15 * 1000)
                    .retryPolicy(retryPolicy)
                    .namespace(ConnectConstant.NAMESPACE)
                    .build();

            createDefaultNode();

            client2.start();
            return client2;
        } catch (Exception e) {
            log.error("Failed to connect to zookeeper", e);
        }

        return null;
    }

    private void createDefaultNode() {
        try {
            String path = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(ConnectConstant.NODE_DEFAULT_PATH);

            log.debug("Create default node: {}", path);
        } catch (Exception e) {
            log.error("Failed to create default node", e);
        }
    }
}
