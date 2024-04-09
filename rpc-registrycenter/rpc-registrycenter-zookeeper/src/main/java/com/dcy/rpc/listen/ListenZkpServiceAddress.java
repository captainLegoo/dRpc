package com.dcy.rpc.listen;

import com.dcy.rpc.cache.ZkpCache;
import com.dcy.rpc.constant.ConnectConstant;
import com.dcy.rpc.registry.Watcher;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author Kyle
 * @date 2024/04/09
 * <p>
 * listen address change
 */
@Slf4j
public class ListenZkpServiceAddress implements Watcher, Runnable {

    private final String clientAddress;

    private final CuratorFramework client;

    private final Set<String> proxyNameCacheSet;

    private final Map<String, List<InetSocketAddress>> serviceAddressMap;

    public final Map<InetSocketAddress, Channel> channelMap;

    public ListenZkpServiceAddress(String clientAddress, Set<String> proxyNameCacheSet, Map<String, List<InetSocketAddress>> serviceAddressMap, Map<InetSocketAddress, Channel> channelMap) {
        this.clientAddress = clientAddress;
        this.client = ZkpCache.CLIENT_CACHE.get(clientAddress);
        this.proxyNameCacheSet = proxyNameCacheSet;
        this.serviceAddressMap = serviceAddressMap;
        this.channelMap = channelMap;
    }

    @Override
    public void run() {
        AddressUpAndDownWatcher();
    }

    @Override
    public void AddressUpAndDownWatcher() {
        Iterator<String> iterator = proxyNameCacheSet.iterator();
        while (iterator.hasNext()) {
            String serviceName = iterator.next();
            if (!serviceAddressMap.containsKey(serviceName)) {
                continue;
            }

            try {
                boolean flag = false;

                List<InetSocketAddress> activeAddressList = serviceAddressMap.get(serviceName);

                String nodePath = ConnectConstant.NODE_DEFAULT_PATH + "/" + serviceName;
                List<InetSocketAddress> addressFromZkpList = convertToSocketAddressList(client.getChildren().forPath(nodePath));

                // offline
                for (InetSocketAddress address : activeAddressList.toArray(new InetSocketAddress[0])) {
                    boolean contains = addressFromZkpList.contains(address);
                    if (!contains) {
                        if (Objects.isNull(channelMap.get(address)) || !channelMap.get(address).isActive()) {
                            flag = false;
                            log.debug("ListenZkpServiceAddress address remove -> {}", address);
                            activeAddressList.remove(address);
                        }
                    }
                }

                // online
                for (InetSocketAddress address : addressFromZkpList) {
                    boolean contains = activeAddressList.contains(address);
                    if (!contains) {
                        if (Objects.nonNull(channelMap.get(address))) {
                            flag = true;
                            log.debug("ListenZkpServiceAddress address add -> {}", address);
                            activeAddressList.add(address);
                        }
                    }
                }

                if (flag) {
                    // update
                    serviceAddressMap.put(serviceName, activeAddressList);
                    System.out.println("update serviceAddressMap: " + serviceAddressMap);
                    // TODO need loadbalancer
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<InetSocketAddress> convertToSocketAddressList(List<String> addresses) {
        List<InetSocketAddress> socketAddressList = new ArrayList<>();
        for (String address : addresses) {
            String[] parts = address.split(":");
            if (parts.length == 2) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                socketAddressList.add(new InetSocketAddress(host, port));
            } else {
                throw new IllegalArgumentException("Invalid address format: " + address);
            }
        }
        return socketAddressList;
    }
}