package com.dcy.rpc.config;

import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.util.IdGenerator;
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
    private RegistryConfig registryConfig;
    private SerializeTypeEnum serializableType;
    private CompressTypeEnum compressType;
    private final IdGenerator idGenerator = new IdGenerator(1,2);
}
