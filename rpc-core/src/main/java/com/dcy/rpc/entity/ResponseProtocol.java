package com.dcy.rpc.entity;

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
    private byte serializeTypeId;
    private byte compressTypeId;
    private long timeStamp;
    private Object responseBody;
}
