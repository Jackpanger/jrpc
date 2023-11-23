package com.jackpang;

import com.jackpang.annotation.JrpcApi;
import com.jackpang.channelHandler.handler.JrpcRequestDecoder;
import com.jackpang.channelHandler.handler.JrpcResponseEncoder;
import com.jackpang.channelHandler.handler.MethodCallHandler;
import com.jackpang.core.HeartbeatDetector;
import com.jackpang.discovery.Registry;
import com.jackpang.discovery.RegistryConfig;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.loadBalancer.impl.RoundRobinLoadBalancer;
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

    // declare basic configuration
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;


    public static final int PORT = 8089;
    public static LoadBalancer LOAD_BALANCER;
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1L, 1L);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";

    public static final ThreadLocal<JrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    @Getter
    private Registry registry;

    // connection cache
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>();

    // record the service published by the provider
    public static final Map<String, ServiceConfig> SERVERS_LIST = new ConcurrentHashMap<>(16);

    // global pending completable future
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private JrpcBootstrap() {
        // Initialization when constructing the bootstrap.
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
        this.appName = appName;
        return this;
    }

    /**
     * Configure the registration center.
     *
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        JrpcBootstrap.LOAD_BALANCER = new RoundRobinLoadBalancer();
        return this;
    }

    /**
     * Configure the protocol.
     *
     * @param protocolConfig protocol configuration
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("Current protocolConfig:{}", protocolConfig.toString() + "protocol");
        }
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
        registry.register(service);
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
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
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

        reference.setRegistry(registry);
        return this;
    }

    public JrpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if (log.isDebugEnabled()) {
            log.debug("Current serializeType:[{}]", serializeType);
        }
        return this;
    }

    public JrpcBootstrap compress(String compressType) {
        COMPRESS_TYPE = compressType;
        if (log.isDebugEnabled()) {
            log.debug("Current compressType:[{}]", compressType);
        }
        return this;
    }

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
            for (Class<?> anInterface : interfaces) {
                ServiceConfig serviceConfig = new ServiceConfig();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
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
}
