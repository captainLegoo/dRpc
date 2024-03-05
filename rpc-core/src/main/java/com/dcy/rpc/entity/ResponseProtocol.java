package com.dcy.rpc.entity;

import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Kyle
 * @date 2024/02/28
 *
 * Response Protocol
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProtocol {
    private long requestId;
    private byte code; // 1: success 2:error
    private SerializeTypeEnum serializeTypeId;
    private CompressTypeEnum compressTypeId;
    private long timeStamp;
    private Object responseBody;
}
