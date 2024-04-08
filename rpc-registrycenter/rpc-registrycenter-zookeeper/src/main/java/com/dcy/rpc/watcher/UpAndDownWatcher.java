package com.dcy.rpc.watcher;

import com.dcy.rpc.cache.ZkpCache;
import com.dcy.rpc.constant.ConnectConstant;
import com.dcy.rpc.registry.Watcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/04/06
 * <p>
 * dynamic detection of address
 *
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    // TODO When this method is executed, the zookeeper client is created but the connection is not actually constructed.
    //  causing the method to fail to run

    private String host;
    private int port;
    private final CuratorFramework client;
    private final Set<String> serviceNameSet;

    public UpAndDownWatcher(String host, int port, Set<String> serviceNameSet) {
        this.host = host;
        this.port = port;
        this.serviceNameSet = serviceNameSet;
        // init client from cache
        this.client = ZkpCache.CLIENT_CACHE.get(host + ":" + host);
    }

    @Override
    public void AddressUpAndDownWatcher() {
        if (this.client == null) {
            return;
        }
        Iterator<String> iterator = serviceNameSet.iterator();
        while (iterator.hasNext()) {
            // get service name
            String serviceName = iterator.next();
            // get service path
            String servicePath = ConnectConstant.NODE_DEFAULT_PATH + "/" + serviceName;
            //
            dynamicDetection(servicePath);
        }
    }

    private void dynamicDetection(String servicePath) {
        log.debug("servicePath is -> {}", servicePath);
        try {
            // Create a PathChildrenCache to monitor changes in the specified node
            PathChildrenCache pathChildrenCache = new PathChildrenCache(this.client, servicePath, true);
            pathChildrenCache.start(true);
            // Register a listener to monitor node data changes
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    System.out.println("子节点变化~~~");
                    System.out.println("event = " + event);
                    // Monitor the data changes of child nodes and get the changed data
                    PathChildrenCacheEvent.Type type = event.getType();
                    if (type.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                        String data = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/"));
                        System.out.println("update data: " + new String(data));
                    }
                    if (type.equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                        String data = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/"));
                        System.out.println("add data: " + new String(data));
                    }
                    if (type.equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                        String data = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                        System.out.println("remove data: " + new String(data));
                    }
                }
            });

            // 循环等待程序终止
            Thread.currentThread().join();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
