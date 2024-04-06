package com.dcy.rpc.strategy;

import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.serialize.jdk.JdkSerialize;
import com.dcy.rpc.serialize.Serialize;
import com.dcy.rpc.serialize.protostuff.ProtostuffSerialize;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2024/03/04
 * <p>
 * Serialize Strategy
 */
@Slf4j
public class SerializeStrategy {
    private final static Map<SerializeTypeEnum, Serialize> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);
    private final static Map<Byte, Serialize> SERIALIZER_Id_CACHE = new ConcurrentHashMap<>(8);

    static {
        Serialize jdkSerializer = new JdkSerialize();
        SERIALIZER_CACHE.put(SerializeTypeEnum.JDK, jdkSerializer);
        SERIALIZER_Id_CACHE.put(SerializeTypeEnum.JDK.getSerializeId(), jdkSerializer);

        Serialize protostuffSerialize = new ProtostuffSerialize();
        SERIALIZER_CACHE.put(SerializeTypeEnum.PROTOSTUFF, protostuffSerialize);
        SERIALIZER_Id_CACHE.put(SerializeTypeEnum.PROTOSTUFF.getSerializeId(), protostuffSerialize);
    }

    /**
     * Get the serializer from the cache through the factory method
     * @param serializeTypeEnum
     * @return
     */
    public static Serialize getSerializer(SerializeTypeEnum serializeTypeEnum) {
        Serialize serialize = SERIALIZER_CACHE.get(serializeTypeEnum);
        if (serialize == null) {
            log.error("The configured serialization method was not found, and the default method will be used.");
            serialize = SERIALIZER_CACHE.get(SerializeTypeEnum.JDK);
        }
        return serialize;
    }
    /**
     * Get the serializer by id from the cache through the factory method
     * @param serializeTypeEnumId
     * @return
     */
    public static Serialize getSerializerById(byte serializeTypeEnumId) {
        Serialize serialize = SERIALIZER_Id_CACHE.get(serializeTypeEnumId);
        if (serialize == null) {
            log.error("The configured serialization method was not found, and the default method will be used.");
            serialize = SERIALIZER_Id_CACHE.get(SerializeTypeEnum.JDK.getSerializeId());
        }
        return serialize;
    }

}
