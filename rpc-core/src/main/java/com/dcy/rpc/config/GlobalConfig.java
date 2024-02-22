package com.dcy.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kyle
 * @date 2024/02/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig {
    private String bootstrapName;
    private int port;
}
