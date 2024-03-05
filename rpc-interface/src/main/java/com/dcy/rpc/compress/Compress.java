package com.dcy.rpc.compress;

/**
 * @author Kyle
 * @date 2023/03/05
 * <p>
 * Compression interface
 */
public interface Compress {
    /**
     * compression
     *
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);

    /**
     * decompression
     *
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
