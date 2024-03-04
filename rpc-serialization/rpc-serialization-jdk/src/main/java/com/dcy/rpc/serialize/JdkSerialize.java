package com.dcy.rpc.serialize;

import com.dcy.rpc.Serialize;
import com.dcy.rpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author Kyle
 * @date 2024/03/04
 *
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

        // Object serialized into byte array
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            if (log.isDebugEnabled()) {
                log.debug("The object is serialized using jdk【{}】, and the serialized bytes are【{}】", object, baos.toByteArray().length);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Exception when using jdk serializing object【{}】", object);
            throw new SerializeException(e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

        // Convert byte array to object serialization
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            if (log.isDebugEnabled()) {
                log.debug("The object is deserialized using jdk【{}】", bytes);
            }
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Exception when using jdk deserialized object【{}】", bytes);
            throw new SerializeException(e);
        } finally {
            try {
                ois.close();
                bais.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

