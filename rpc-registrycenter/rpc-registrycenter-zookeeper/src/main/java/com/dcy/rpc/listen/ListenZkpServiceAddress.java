package com.dcy.rpc.listen;

import com.dcy.rpc.cache.ZkpCache;
import com.dcy.rpc.constant.ConnectConstant;
import com.dcy.rpc.registry.Registry;
import com.dcy.rpc.registry.Watcher;
import org.apache.curator.framework.CuratorFramework;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/04/09
 * <p>
 * listen address change
 */
public class ListenZkpServiceAddress implements Watcher, Runnable {

    private final String clientAddress;

    private final CuratorFramework client;

    private final Set<String> proxyNameCacheSet;

    public ListenZkpServiceAddress(String clientAddress, Set<String> proxyNameCacheSet) {
        this.clientAddress = clientAddress;
        this.client = ZkpCache.CLIENT_CACHE.get(clientAddress);
        this.proxyNameCacheSet = proxyNameCacheSet;
    }

    @Override
    public void run() {
        AddressUpAndDownWatcher();
    }

    @Override
    public void AddressUpAndDownWatcher() {
        Iterator<String> iterator = proxyNameCacheSet.iterator();
        while (iterator.hasNext()) {
            String nodePath = ConnectConstant.NODE_DEFAULT_PATH + "/" + iterator.next();
            try {
                List<String> list = client.getChildren().forPath(nodePath);
                System.out.println("nodePath = " + list);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
