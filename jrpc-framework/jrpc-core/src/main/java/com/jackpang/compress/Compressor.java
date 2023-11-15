package com.jackpang.compress;

/**
 * description: Compressor
 * date: 11/14/23 9:13 PM
 * author: jinhao_pang
 * version: 1.0
 */
public interface Compressor {
    /**
     * compress
     * @param bytes bytes to be compressed
     * @return compressed bytes
     */
    byte[] compress(byte[] bytes);

    /**
     * decompress
     * @param bytes bytes to be decompressed
     * @return decompressed bytes
     */
    byte[] decompress(byte[] bytes);

}
