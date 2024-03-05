package com.dcy.rpc.strategy;

import com.dcy.rpc.compress.Compress;
import com.dcy.rpc.compress.DeflateCompressor;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2023/10/4 11:29
 * <p>
 * Compression factory method class
 */
@Slf4j
public class CompressStrategy {

    private final static Map<CompressTypeEnum, Compress> COMPRESS_CACHE = new ConcurrentHashMap<>(8);


    static {
        Compress deflateCompressor = new DeflateCompressor();
        COMPRESS_CACHE.put(CompressTypeEnum.DEFLATE, deflateCompressor);

    }

    /**
     * Get the compressor from the cache through the factory method
     * @param compressTypeEnum
     * @return
     */
    public static Compress getCompress(CompressTypeEnum compressTypeEnum) {
        Compress compress = COMPRESS_CACHE.get(compressTypeEnum);
        if (compress == null) {
            compress = COMPRESS_CACHE.get(CompressTypeEnum.DEFLATE.getCompressId());
            log.error("The configured compressor was not found, and the default will be used.");
        }
        return compress;
    }
}
