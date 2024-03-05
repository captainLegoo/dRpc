package com.dcy.rpc.compress;

import com.dcy.rpc.exception.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author Kyle
 * @date 2023/03/05
 * <p>
 * Implementation of deflate compression
 */
@Slf4j
public class DeflateCompressor implements Compress {
    /**
     * compression
     *
     * @param bytes
     * @return
     */
    @Override
    public byte[] compress(byte[] bytes) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            Deflater deflater = new Deflater();
            deflater.setInput(bytes);
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            deflater.end();
            byte[] result = outputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Use Deflate to compress the byte array, and the length【{}】is compressed to【{}】", bytes.length, result.length);
            }
            return result;
        } catch (Exception e) {
            log.error("Exception occurred while compressing byte array", e);
            throw new CompressException(e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * decompression
     *
     * @param bytes
     * @return
     */
    @Override
    public byte[] decompress(byte[] bytes) {
        ByteArrayOutputStream outputStream = null;
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(bytes);
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && inflater.needsInput()) {
                    break;
                }
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            byte[] result = outputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Decompress the byte array, and decompress the length【{}】to【{}】", bytes.length, result.length);
            }
            return result;
        } catch (DataFormatException e) {
            throw new CompressException(e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
