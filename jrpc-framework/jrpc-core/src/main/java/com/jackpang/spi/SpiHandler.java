package com.jackpang.spi;

import com.jackpang.config.ObjectWrapper;
import com.jackpang.exceptions.SpiException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * description: SpiHandler
 * Simple version of SPI
 * date: 11/23/23 8:27 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class SpiHandler {
    // define base path of spi
    private static final String BASE_PATH = "META-INF/services";

    // define cache map to store spi instance
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(8);
    // define cache map to store spi implement
    public static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>(32);

    // load spi configuration
    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> value = getImplNames(child);
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }

    private static List<String> getImplNames(File child) {
        try (FileReader fileReader = new FileReader(child);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
                implNames.add(line);
            }
            return implNames;
        } catch (IOException e) {
            log.error("An error occurred in reading spi file", e);
        }
        return null;
    }


    public static <T> ObjectWrapper<T> get(Class<T> clazz) {

        List<ObjectWrapper<T>> list = getList(clazz);
        return list == null ? null : list.get(0);
    }

    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {

        // 1. get spi implement from cache
        List<ObjectWrapper<?>> cache = SPI_IMPLEMENT.get(clazz);
        if (cache != null && !cache.isEmpty()) {
            return cache.stream().map(wrapper->(ObjectWrapper<T>) wrapper).collect(Collectors.toList());
        }
        // 2. establish spi instance
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if (implNames == null || implNames.isEmpty()) {
            return null;
        }

        // initialize spi instance
        List<ObjectWrapper<?>> impls = new ArrayList<>();
        for (String implName : implNames) {
            try {
                String[] codeTypeName = implName.split("-");
                if (codeTypeName.length != 3) {
                    throw new SpiException("Spi file illegal");
                }
                Byte code = Byte.valueOf(codeTypeName[0]);
                String type = codeTypeName[1];
                String implementName = codeTypeName[2];

                Class<?> aClass = Class.forName(implementName);
                Object object = aClass.getConstructor().newInstance();
                ObjectWrapper<?> wrapper = new ObjectWrapper<>(code, type, object);
                impls.add(wrapper);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                log.error("An error occurred in initializing spi instance", e);
            }
        }
        SPI_IMPLEMENT.put(clazz, impls);

        return impls.stream().map(wrapper->(ObjectWrapper<T>) wrapper).collect(Collectors.toList());
    }

}
