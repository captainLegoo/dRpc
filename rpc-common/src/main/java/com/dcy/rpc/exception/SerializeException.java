package com.dcy.rpc.exception;

/**
 * @author Kyle
 * @date 2024/03/04
 * <p>
 * Exception catching classes for serializers
 */
public class SerializeException extends RuntimeException {
    public SerializeException() {
        super();
    }

    public SerializeException(String message) {
        super(message);
    }

    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException(String message, Throwable cause) {
        super(message, cause);
    }
}