package com.dcy.rpc.listen;

import com.dcy.rpc.cache.ZkpCache;
import com.dcy.rpc.constant.ConnectConstant;
import com.dcy.rpc.constant.EventType;
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
public class ListenZkpServiceAddress2 implements Runnable{

    private final String registryCenterAddress;
    private final Map<String, List<InetSocketAddress>> pendingRemoveAddressMap;

    public ListenZkpServiceAddress2(String registryCenterAddress, Map<String, List<InetSocketAddress>> pendingRemoveAddressMap) {
        this.registryCenterAddress = registryCenterAddress;
        this.pendingRemoveAddressMap = pendingRemoveAddressMap;
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
                            log.debug("It is detected that an address is online...");
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
