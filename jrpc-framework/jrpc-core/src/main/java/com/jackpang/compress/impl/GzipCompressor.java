package com.jackpang.compress.impl;

import com.jackpang.compress.Compressor;
import com.jackpang.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * description: GzipCompressor
 * date: 11/14/23 9:17 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            byte[] result = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("Compress success, origin size:{}, compressed size:{}", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("Compress error:{}", e.getMessage());
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try ( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
              GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);) {
            byte[] result = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) {
                log.debug("Decompress success, origin size:{}, decompressed size:{}", bytes.length, result.length);
            }
            return result;
        } catch (IOException e) {
            log.error("Decompress error:{}", e.getMessage());
            throw new CompressException(e);
        }
    }
}
