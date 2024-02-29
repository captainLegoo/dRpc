package com.dcy.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Kyle
 * @date 2024/02/28
 *
 * Request Payload
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    private String interfaceName;
    private String methodName;
    private Class<?>[] parametersType;
    private Object[] parameterValue;
    private Class<?> returnType;
}
