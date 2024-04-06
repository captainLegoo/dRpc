package com.dcy.rpc.serialize.protostuff;


import com.dcy.rpc.serialize.Serialize;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/04/06
 * <p>
 * Serialization using protostuff
 * TODO: can not deserialize the object.class
 */
@Slf4j
public class ProtostuffSerialize implements Serialize {

    /**
     * Serialization
     * @param object Object instance to be serialized
     * @return
     */
    @Override
    public byte[] serializer(Object object) {
        Schema schema = RuntimeSchema.getSchema(object.getClass());
        byte[] bytes = ProtostuffIOUtil.toByteArray(object, schema,
                LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
        log.debug("The object is serialized using protostuff【{}】, and the serialized bytes are【{}】", object, bytes.length);
        return bytes;

    }

    /**
     * Deserialization
     * @param bytes Byte array to be deserialized
     * @param clazz The class object of the target class
     * @param <T>
     * @return
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            T message = clazz.newInstance();
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        ProtostuffSerialize protostuffSerialize = new ProtostuffSerialize();
        Object object = "123";
        byte[] serializered = protostuffSerialize.serializer(object);

        Object deserialize = protostuffSerialize.deserialize(serializered, Object.class);
        System.out.println("deserialize = " + deserialize);
    }
}
