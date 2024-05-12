package com.dcy.rpc.listen;

import com.dcy.rpc.cache.ZkpCache;
import com.dcy.rpc.constant.ConnectConstant;
import com.dcy.rpc.constant.EventType;
import com.dcy.rpc.constant.IPAddressConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Kyle
 * @date 2024/04/11
 * <p>
 * Use Curator's automatic monitoring mechanism to monitor registered node information
 */
@Slf4j
public class ListenZkpServiceAddress implements Runnable{

    private final String registryCenterAddress;
    private final Map<String, List<InetSocketAddress>> pendingRemoveAddressMap;
    private final Map<String, List<InetSocketAddress>> pendingAddAddressMap;

    public ListenZkpServiceAddress(String registryCenterAddress, Map<String, List<InetSocketAddress>> pendingRemoveAddressMap, Map<String, List<InetSocketAddress>> pendingAddAddressMap) {
        this.registryCenterAddress = registryCenterAddress;
        this.pendingRemoveAddressMap = pendingRemoveAddressMap;
        this.pendingAddAddressMap = pendingAddAddressMap;
    }

    @Override
    public void run() {
        log.info("Start monitoring service address changes....");
        listenAddress();
    }

    public void listenAddress() {

        CuratorFramework client = ZkpCache.CLIENT_CACHE.get(registryCenterAddress);

        try {
            // create a NodeCache to monitor the change of node
            TreeCache treeCache = new TreeCache(client, ConnectConstant.NODE_DEFAULT_PATH);

            // register listener
            treeCache.getListenable().addListener(new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent event) throws Exception {
                    if (Objects.nonNull(event.getData())) {
                        TreeCacheEvent.Type eventType = event.getType();
                        String path = event.getData().getPath();

                        if (eventType.toString().equals(EventType.NODE_REMOVED.getTypeInfo())) {
                            String serviceName = getServiceName(path);
                            InetSocketAddress address = getAddress(path);
                            log.debug("Detected that Address 【{}】 is going offline...", address);
                            List<InetSocketAddress> addressList = pendingRemoveAddressMap.getOrDefault(serviceName, new ArrayList<>());
                            addressList.remove(address);
                            if (addressList.isEmpty()) {
                                pendingRemoveAddressMap.remove(serviceName);
                            }
                            log.debug("Address 【{}】 has been successfully removed...", address);
                        } else if (eventType.toString().equals(EventType.NODE_ADDED.getTypeInfo())) {
                            String serviceName = getServiceName(path);
                            if (isContainIpAddress(path)) {
                                InetSocketAddress address = getAddress(path);
                                // Add service and node in pending map cache
                                List<InetSocketAddress> addressList = pendingAddAddressMap.getOrDefault(serviceName, new ArrayList<>());
                                if (addressList.contains(address)) {
                                    log.debug("Address 【{}】 has been added to the pending map cache...", address);
                                } else {
                                    addressList.add(address);
                                    log.debug("Address 【{}】 has been successfully added to the pending map cache...", address);
                                }
                                pendingAddAddressMap.putIfAbsent(serviceName, addressList);
                            }
                        }
                    }
                }
            });

            // 3.start monitoring
            treeCache.start();

            // Loop waiting for program to terminate
            Thread.currentThread().join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isContainIpAddress(String path) {
        String lastAddressPath = path.substring(path.lastIndexOf("/") + 1);
        if (lastAddressPath.contains(":")) {
            log.error("isContainIpAddress-lastAddressPath => {}", lastAddressPath);
            String[] str = lastAddressPath.split(":");
            if (str.length != 2) {
                return false;
            }
            boolean matchesIpAddress = str[0].matches(IPAddressConstant.IPADDRESS_PATTERN);
            int portNumber = Integer.parseInt(str[1]);
            boolean matchesIpPort = portNumber >= 0 && portNumber <= 65535;
            return matchesIpAddress && matchesIpPort;
        }
        return false;
    }

    private String getServiceName(String path) {
        int lastIndexOfSlash = path.lastIndexOf("/");
        String serviceName = path.substring(ConnectConstant.NODE_DEFAULT_PATH.length() + 1, lastIndexOfSlash);
        //log.debug("serviceName -> {}", serviceName);
        return serviceName;
    }

    private InetSocketAddress getAddress(String path) {
        String[] addressSplit = path.substring(path.lastIndexOf("/") + 1).split(":");
        InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));
        //log.debug("address = " + address);
        return address;
    }
}
