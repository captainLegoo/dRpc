package com.dcy.rpc.enumeration;

import lombok.Getter;

/**
 * @author Kyle
 * @date 2023/9/29 15:17
 *
 * Enumeration class that marks the request type
 */
@Getter
public enum RequestTypeEnum {

    REQUEST((byte) 1, "General request"),
    HEART((byte) 2, "Heartbeat detection");

    private byte id;

    private String type;

    RequestTypeEnum(byte id, String type) {
        this.id = id;
        this.type = type;
    }

}