package com.dcy.rpc.constant;

/**
 * @author Kyle
 * @date 2024/04/24
 * <p>
 * zkp node event type
 */
public enum EventType {
    NODE_ADDED("NODE_ADDED"),
    NODE_UPDATED("NODE_UPDATED"),
    NODE_REMOVED("NODE_REMOVED"),
    ;

    private String typeInfo;

    EventType(String typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String getTypeInfo(){
        return typeInfo;
    }
}
