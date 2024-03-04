package com.dcy.rpc.serialize;

import com.dcy.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author Kyle
 * @date 2024/03/04
 * <p>
 * Serialization using jdk
 */
@Slf4j
public class JdkSerialize implements Serialize {

    /**
     * Serialization
     * @param object Object instance to be serialized
     * @return byte array
     */
    @Override
    public byte[] serializer(Object object) {
        if (object == null) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("The object is serialized using jdk【{}】, and the serialized bytes are【{}】", object, bytes.length);
            }
            return bytes;
        } catch (IOException e) {
            log.error("Exception when using jdk serializing object【{}】", object, e);
            throw new SerializeException("Error during serialization", e);
        }
    }

    /**
     * Deserialization
     * @param bytes Byte array to be deserialized
     * @param clazz The class object of the target class
     * @return target instance
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || clazz == null) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object object = ois.readObject();
            if (log.isDebugEnabled()) {
                log.debug("The object is deserialized using jdk【{}】", bytes);
            }
            return clazz.cast(object);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Exception when using jdk deserialized object【{}】", bytes, e);
            throw new SerializeException("Error during deserialization", e);
        }
    }
}


