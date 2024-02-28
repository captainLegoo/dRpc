package com.dcy.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Kyle
 * @date 2023/10/1 15:05
 *
 * Enumerated classes of serialized types
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SerializeTypeEnum {

    JDK((byte)1, "jdk"),
    JSON((byte)2, "json"),
    HESSIAN((byte)3, "hessian");

    private byte serializeId;
    private String des;
}
