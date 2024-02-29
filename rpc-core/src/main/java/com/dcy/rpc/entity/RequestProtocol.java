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
 * Request Protocol
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestProtocol {
    private long requestId;
    private byte requestType;
    private byte serializeTypeId;
    private byte compressTypeId;
    private long timeStamp;
    private RequestPayload requestPayload;
}
