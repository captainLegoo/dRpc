package com.dcy.rpc.task;

import com.dcy.rpc.cache.ProxyCache;
import com.dcy.rpc.registry.Watcher;
import com.dcy.rpc.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author Kyle
 * @date 2024/04/07
 * <p>
 * scheduled task for doing
 * - dynamic address refresh
 */
@Slf4j
public class ScheduledTask implements Runnable{

    private String host;
    private int port;

    public ScheduledTask(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        //while (true) {
            log.debug("ScheduledTask time -> {}", new Date());
            Watcher watcher = new UpAndDownWatcher(host, port, ProxyCache.PROXY_NAME_CACHE_SET);
            watcher.AddressUpAndDownWatcher();
        //}
    }
}
