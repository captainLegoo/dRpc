package com.dcy.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Kyle
 * @date 2023/10/4 15:17
 *
 * Enumerated classes of compress types
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CompressTypeEnum {

    GZIP((byte)1, "gzip"),
    DEFLATE((byte)2, "deflate");

    private byte compressId;
    private String des;
}
