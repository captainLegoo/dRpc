package com.dcy.rpc.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Kyle
 * @date 2024/04/06
 */
public class AddressCache {
    public static final Set<String> SERVICE_ADDRESS_DETECTION_CACHE = new ConcurrentSkipListSet<>();
}
