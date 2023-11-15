package com.jackpang.compress;

import com.jackpang.serialize.Serializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: SerializerWrapper
 * date: 11/5/23 5:24 PM
 * author: jinhao_pang
 * version: 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompressorWrapper {
    private byte code;
    private String type;
    private Compressor compressor;
}
