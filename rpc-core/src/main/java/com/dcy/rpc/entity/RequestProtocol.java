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
    private SerializeTypeEnum serializeType;
    private CompressTypeEnum compressType;
    private long timeStamp;
    private RequestPayload requestPayload;
}
