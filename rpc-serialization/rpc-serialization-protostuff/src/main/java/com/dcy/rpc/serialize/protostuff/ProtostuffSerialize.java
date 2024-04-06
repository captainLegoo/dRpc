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
     * @param c     The class object of the target class
     * @param <T>
     * @return
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> c) {
        T t = null;
        try {
            t = c.newInstance();
            Schema schema = RuntimeSchema.getSchema(t.getClass());
            ProtostuffIOUtil.mergeFrom(bytes, t, schema);
            log.debug("The object is deserialized using protostuff【{}】", bytes);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            log.error("Exception when using protostuff deserialized object【{}】", bytes, e);
        }
        return t;
    }
}
