package com.dcy.rpc.registry;

import com.dcy.rpc.constant.ConnectConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.List;

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
            // Check if service path exists
            Stat servicePathStat = client.checkExists().forPath(servicePath);
            if (servicePathStat == null) {
                // If service path does not exist, create it
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(servicePath);
            }


            // Check if ip of service path exists
            Stat ipAddressPathStat = client.checkExists().forPath(ipAddressPath);
            if (ipAddressPathStat == null) {
                // If ip of service path does not exist, create it
                String finalPath = client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(ipAddressPath);

                return finalPath.equals(ipAddressPath);
            } else {
                // If ip of service path already exists, handle accordingly
                log.warn("IP address path {} already exists", ipAddressPath);
                return false; // Or handle as per your business logic
            }

        } catch (KeeperException.NodeExistsException e) {
            log.warn("Node {} already exists", e.getPath());
        } catch (Exception e) {
            log.error("Failed to register service", e);
        }
        return false;
    }

    @Override
    public String lookupAddress(String serviceName) {
        String servicePath = ConnectConstant.NODE_DEFAULT_PATH + "/" + serviceName;
        try {
            List<String> pathList = client.getChildren().forPath(servicePath);
            return pathList.get(0);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private CuratorFramework connectZookeeper(String address, int host) {

        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);

            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(address + ":" + host)
                    .sessionTimeoutMs(60 * 1000)
                    .connectionTimeoutMs(15 * 1000)
                    .retryPolicy(retryPolicy)
                    .namespace(ConnectConstant.NAMESPACE)
                    .build();

            createDefaultNode();

            client.start();
            return client;
        } catch (Exception e) {
            log.error("Failed to connect to zookeeper", e);
        }

        return null;
    }

    private void createDefaultNode() {
        try {
            if (client != null) { // 避免空指针异常
                String path = client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(ConnectConstant.NODE_DEFAULT_PATH);

                log.debug("Create default node: {}", path);
            } else {
                log.error("CuratorFramework client is null. Failed to create default node.");
            }
        } catch (Exception e) {
            log.error("Failed to create default node", e);
        }
    }
}
