package com.jackpang.compress;

import com.jackpang.compress.impl.GzipCompressor;
import com.jackpang.serialize.SerializerWrapper;
import com.jackpang.serialize.impl.HessianSerializer;
import com.jackpang.serialize.impl.JdkSerializer;
import com.jackpang.serialize.impl.JsonSerializer;
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
    private final static Map<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        CompressorWrapper gzip = new CompressorWrapper((byte) 1, "jdk", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzip);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzip);

    }

    public static CompressorWrapper getCompressor(String compressorType) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE.get(compressorType);
        if (compressorWrapper == null) {
            if (log.isDebugEnabled()) {
                log.error("Compressor not found, use default gzip compressor");
            }
            return COMPRESSOR_CACHE.get("gzip");
        }
        return compressorWrapper;
    }

    public static CompressorWrapper getCompressor(byte compressorCode) {
        CompressorWrapper compressorWrapper = COMPRESSOR_CACHE_CODE.get(compressorCode);
        if (compressorWrapper == null) {
            if (log.isDebugEnabled()) {
                log.error("Compressor not found, use default gzip compressor");
            }
            return COMPRESSOR_CACHE_CODE.get((byte) 1);
        }
        return compressorWrapper;
    }
}
