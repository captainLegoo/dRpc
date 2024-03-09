package com.dcy.rpc.compress;

import com.dcy.rpc.exception.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Kyle
 * @date 2023/10/4 11:36
 *
 * Implementation of gzip compression
 *
 * TODO not done
 */
@Slf4j
public class GzipCompressor implements Compress {
    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)
        ) {
            gzipOutputStream.write(bytes);
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Use GZIP to compress the byte array, and the length【{}】is compressed to【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("Exception occurred while compressing byte array", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = null;
        GZIPInputStream gzipInputStream = null;

        try {
            bais = new ByteArrayInputStream(bytes);
            gzipInputStream = new GZIPInputStream(bais);
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] result = baos.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Decompress the byte array, and decompress the length【{}】to【{}】", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("Exception occurred while decompressing byte array", e);
            throw new CompressException(e);
        } finally {
            try {
                gzipInputStream.close();
                bais.close();
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
