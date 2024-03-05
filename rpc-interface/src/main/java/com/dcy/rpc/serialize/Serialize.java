package com.dcy.rpc.serialize;

/**
 * @author Kyle
 * @date 2024/03/04
 * <p>
 * serialization interface
 */
public interface Serialize {
    /**
     * Serialization
     * @param object Object instance to be serialized
     * @return byte array
     */
    byte[] serializer(Object object);

    /**
     * Deserialization
     * @param bytes Byte array to be deserialized
     * @param clazz The class object of the target class
     * @param <T>   target class generic
     * @return target instance
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
