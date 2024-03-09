package com.dcy.rpc.config;

import com.dcy.rpc.enumeration.RegistryCenterEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Kyle
 * @date 2024/02/20
 */
@Data
@AllArgsConstructor
public class RegistryConfig {
    private final RegistryCenterEnum registryCenterEnum;
    private final String host;
    private final int port;
}
