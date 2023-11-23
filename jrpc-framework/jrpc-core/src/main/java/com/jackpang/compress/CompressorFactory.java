package com.jackpang.compress;

import com.jackpang.compress.impl.GzipCompressor;
import com.jackpang.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: SerializerFactory
 * date: 11/5/23 5:20 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class CompressorFactory {
    private final static Map<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Compressor> gzip = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);

    }

    public static ObjectWrapper<Compressor> getCompressor(String compressorType) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            if (log.isDebugEnabled()) {
                log.error("Compressor not found, use default gzip compressor");
            }
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    public static ObjectWrapper<Compressor> getCompressor(byte compressorCode) {
        ObjectWrapper<Compressor> compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if (compressorWrapper == null) {
            if (log.isDebugEnabled()) {
                log.error("Compressor not found, use default gzip compressor");
            }
            return COMPRESSOR_CACHE_CODE.get((byte) 1);
        }
        return compressorWrapper;
    }

    /**
     * add compressor to cache
     * @param compressorWrapper compressor wrapper
     */
    public static void addCompressor(ObjectWrapper<Compressor> compressorWrapper) {
        COMPRESSOR_CACHE.put(compressorWrapper.getName(), compressorWrapper);
        COMPRESSOR_CACHE_CODE.put(compressorWrapper.getCode(),compressorWrapper);
    }
}
