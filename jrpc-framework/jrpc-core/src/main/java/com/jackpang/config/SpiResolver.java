package com.jackpang.config;

import com.jackpang.compress.Compressor;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.spi.SpiHandler;

import java.util.List;

/**
 * description: SpiResolver
 * date: 11/23/23 8:22 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class SpiResolver {
    /**
     * load configuration from spi
     *
     * @param configuration configuration context
     */
    public void loadFromSpi(Configuration configuration) {
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrappers = SpiHandler.getList(LoadBalancer.class);
        // put it into factory
        if (loadBalancerWrappers != null && !loadBalancerWrappers.isEmpty()) {
            configuration.setLoadBalancer(loadBalancerWrappers.get(0).getImpl());
        }

        List<ObjectWrapper<Compressor>> compressorWrappers = SpiHandler.getList(Compressor.class);
        if (compressorWrappers != null) {
            compressorWrappers.forEach(CompressorFactory::addCompressor);
        }

        List<ObjectWrapper<Serializer>> serializerWrappers = SpiHandler.getList(Serializer.class);
        if (serializerWrappers != null) {
            serializerWrappers.forEach(SerializerFactory::addSerializer);
        }
    }
}
