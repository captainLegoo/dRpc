package com.dcy.rpc;

/**
 * @author Kyle
 * @date 2024/02/19
 * <p>
 * Provider Starter
 * - singleton: lazy, double-check locking, prevent reflection intrusion
 * config:
 * -
 */
public class ProviderStarter {
    private static volatile ProviderStarter instance;

    private ProviderStarter() {
        if (instance != null) {
            throw new RuntimeException("ProviderStarter is a singleton");
        }
    }

    /**
     * create class instance
     * @return this
     */
    public static ProviderStarter getInstance() {
        if (instance == null) {
            synchronized (ProviderStarter.class) {
                if (instance == null) {
                    instance = new ProviderStarter();
                }
            }
        }
        return instance;
    }
}
