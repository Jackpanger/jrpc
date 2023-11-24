package com.jackpang;

import com.jackpang.annotation.JrpcApi;
import com.jackpang.channelHandler.handler.JrpcRequestDecoder;
import com.jackpang.channelHandler.handler.JrpcResponseEncoder;
import com.jackpang.channelHandler.handler.MethodCallHandler;
import com.jackpang.config.Configuration;
import com.jackpang.core.HeartbeatDetector;
import com.jackpang.discovery.RegistryConfig;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.transport.message.JrpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * description: JrpcBootStrap
 * date: 11/3/23 10:47 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcBootstrap {

    // JrpcBootstrap is a singleton class, so each application has only one instance.
    private static final JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();

    // global configuration center
    @Getter
    private Configuration configuration;


    // store request object for each thread
    public static final ThreadLocal<JrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    // connection cache
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    // record the service published by the provider
    public static final Map<String, ServiceConfig> SERVERS_LIST = new ConcurrentHashMap<>(16);

    // global pending completable future
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private JrpcBootstrap() {
        // Initialization when constructing the bootstrap.
        configuration = new Configuration();
    }

    public static JrpcBootstrap getInstance() {
        return jrpcBootstrap;
    }

    /**
     * Configure the application name.
     *
     * @param appName application name
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap application(String appName) {
        configuration.setAppName(appName);
        return this;
    }

    /**
     * Configure the registration center.
     *
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        return this;
    }

    /**
     * Configure the loadBalancer.
     *
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    /*
     * -------------------API related to service provider-----------------------------
     */

    /**
     * Publish the service.
     *
     * @param service service to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(ServiceConfig service) {
        // service node
        configuration.getRegistryConfig().getRegistry().register(service);
        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * Publish the services in batches.
     *
     * @param service service list to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(List<ServiceConfig> service) {
        service.forEach(this::publish);
        return this;
    }

    /**
     * Start the service provider.
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            // create server bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // core logic
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new JrpcRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new JrpcResponseEncoder());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*
     * -------------------API related to service consumer-----------------------------
     */

    public JrpcBootstrap reference(ReferenceConfig<?> reference) {
        // heartbeat detection
        HeartbeatDetector.detect(reference.getInterface().getName());

        reference.setRegistry(configuration.getRegistryConfig().getRegistry());
        reference.setGroup(this.getConfiguration().getGroup());
        return this;
    }

    public JrpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        if (log.isDebugEnabled()) {
            log.debug("Current serializeType:[{}]", serializeType);
        }
        return this;
    }

    public JrpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        if (log.isDebugEnabled()) {
            log.debug("Current compressType:[{}]", compressType);
        }
        return this;
    }

    /**
     * Scan the package name to get the service.
     * @param packageName package name
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap scan(String packageName) {
        // scan the package name to get the services
        List<String> classNames = getAllClassNames(packageName);
        // get the service interfaces through reflection
        List<Class<?>> classes = classNames.stream().map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter(clazz -> clazz.getAnnotation(JrpcApi.class) != null).collect(Collectors.toList());


        for (Class<?> clazz : classes) {
            // get its interface
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            // get group information
            JrpcApi jrpcApi = clazz.getAnnotation(JrpcApi.class);
            String group = jrpcApi.group();


            for (Class<?> anInterface : interfaces) {
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                if (log.isDebugEnabled()){
                    log.debug("-------> scan finished already, serviceConfig:{} publish", anInterface);
                }
                // publish the services
                publish(serviceConfig);
            }


        }
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        // 1. get absolute path through packageName
        // com.jackpang.xxx.yyy-> E://xxx/xww/sss/com/jackpang/xxx/yyy
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("package not found during scanning");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private void recursionFile(String absolutePath, List<String> classNames, String basePath) {
        // 1. get the file object
        File file = new File(absolutePath);
        // check if the file is a directory
        if (file.isDirectory()) {
            // find files in the directory
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null) {
                return;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    String name = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(name);
                }
            }

        } else {
            // file -> file name
            String name = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(name);
        }
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        //Users/jack/IdeaProjects/starter/jrpc/jrpc/jrpc-framework/jrpc-core/target/classes/com/jackpang/serialize/SerializerWrapper.class
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath)).replaceAll("/", "\\.");
        return fileName.substring(0, fileName.lastIndexOf(".class"));
    }

    public static void main(String[] args) {
        List<String> allClassNames = JrpcBootstrap.getInstance().getAllClassNames("com.jackpang");
        System.out.println(allClassNames);
    }

    public JrpcBootstrap group(String group) {
        this.configuration.setGroup(group);
        return this;
    }
}
