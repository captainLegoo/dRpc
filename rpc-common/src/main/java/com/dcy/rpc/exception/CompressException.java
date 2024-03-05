package com.dcy.rpc.exception;

/**
 * @author Kyle
 * @date 2023/03/05
 * <p>
 * Exception catching classes for compress
 */
public class CompressException extends RuntimeException {
    public CompressException() {
        super();
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
