# JRPC
## 1 简介 Introduction

本文档描述了我们开发的RPC远程调用框架的设计和架构。我们的框架旨在简化分布式系统中的远程调用过程，提供高效、可靠和易用的服务调用方式

This document describes the design and architecture of the RPC remote calling framework we developed. Our framework is designed to simplify the process of remote calling in distributed systems, providing an efficient, reliable, and easy-to-use service calling method.

## 2 框架概述 Framework Overview

我们的RPC远程调用框架包括以下组件:

+ 通信层: 负责处理网络通信，实现请求的传输和响应的接收。我们选择使用TCP协议进行通信，使用Netty作为网络库。
+ 序列化层:负责将请求和响应进行序列化和反序列化，使其在网络传输中进行编码和解码。我们提供了支持常见序列化方式 (如jdk，JSON、Protobuf等)的插件机制，同时也支持自定义序列化方式
+ 服务注册与发现层:负责服务的注册和发现，使客户端可以通过服务名称来发现可用的服务节点。我们使用个中心化的服务注册中心来管理服务的注册和发现。
+ 负载均衡层:负责将请求合理地分配给服务节点，实现负载均衡。我们支持常见的负载均衡策略，如随机、轮询、加权等。
+ 远程代理。在客户端和服务端实现远程代理，封装远程调用的细节，使开发者可以像本地调用一样调用远程服务。

Our RPC remote calling framework includes the following components:

+ Communication layer: Responsible for handling network communications, implementing the transmission of requests and the reception of responses. We choose to use the TCP protocol for communication and Netty as the network library.
+ Serialization layer: Responsible for serializing and deserializing requests and responses, enabling them to be encoded and decoded during network transmission. We provide a plugin mechanism that supports common serialization methods (such as JDK, JSON, Protobuf, etc.) and also supports custom serialization methods.
+ Service registration and discovery layer: Responsible for the registration and discovery of services, enabling clients to discover available service nodes by service name. We use a centralized service registry to manage the registration and discovery of services.
+ Load balancing layer: Responsible for reasonably distributing requests to service nodes to achieve load balancing. We support common load-balancing strategies, such as random, round-robin, weighted, etc.
+ Remote proxy: Implements remote proxies on the client and server sides, encapsulating the details of remote calls, allowing developers to call remote services as if they were local calls.

## 3 架构设计 Architectural Design

我们的框架采用客户端-服务端架构，以下是框架的整体架构设计:

Our framework adopts a client-server architecture, and the following is the overall architectural design of the framework:

### 3.1 客户端架构 Client Architecture

客户端架构包括以下组件:

+ 远程代理:根据用户定义的接口，生成代理对象，封装远程调用的细节，将本地方法调用转化为远程调用。
+ 序列化器:将请求和响应进行序列化和反序列化，使其能够在网络中进行传输。我们提供了可配置的插件机制，支持多种序列化方式。
+ 负载均衡器:根据负载均衡策略选择合适的服务节点，将请求发送给服务节点。
+ 通信模块: 使用TCP协议进行网络通信，接收服务节点的响应。

The client architecture includes the following components:

- Remote proxy: Generates proxy objects based on user-defined interfaces, encapsulating the details of remote calls, converting local method calls into remote calls.
- Serializer: Serializes and deserializes requests and responses, enabling them to be transmitted over the network. We provide a configurable plugin mechanism that supports multiple serialization methods.
- Load balancer: Chooses an appropriate service node based on the load balancing strategy and sends requests to the service node.
- Communication module: Uses the TCP protocol for network communication, receiving responses from the service node.

### 3.2 服务端架构 Server Architecture

服务端架构包括以下组件:

+ 远程调用处理器:接收来自客户端的请求，根据请求的接口和方法调用相应的服务实现代码，并将结果返回给客户端。
+ 序列化器:将请求和响应进行序列化和反序列化，使其能够在网络中进行传输。我们提供了可配置的插件机制，支持多种序列化方式。
+ 通信模块: 使用TCP协议进行网络通信，接收客户端的请求，并发送服务节点的响应。

The server architecture includes the following components:

- Remote call handler: Receives requests from clients, calls the corresponding service implementation code based on the requested interface and method, and returns the result to the client.
- Serializer: Serializes and deserializes requests and responses, enabling them to be transmitted over the network. We provide a configurable plugin mechanism that supports multiple serialization methods.
- Communication module: Uses the TCP protocol for network communication, receiving requests from the client and sending responses from the service node.

### 3.3 注册中心架构 Registry Architecture

注册中心是中心化的服务注册与发现的管理节点，负责记录和管理可用的服务节点信息，包括服务名称、地址、权重等。客户端和服务端都可以通过注册中心来发现和注册服务。

The registry is a centralized management node for service registration and discovery, responsible for recording and managing available service node information, including service name, address, weight, etc. Both clients and servers can discover and register services through the registry.

## 4 工作流程 Workflow

框架的工作流程如下:

1. 客户端通过远程代理对象调用远程服务.
2. 远程代理将方法调用转化为RPC请求，使用负载均衡策略选择服务节点，并将请求发送给服务节点。
3. 服务节点接收到请求后，使用序列化器对请求进行反序列化，找到对应的服务实现代码，并调用相应的方法。
4. 服务实现完成后，将结果返回给服务节点.
5. 服务节点使用序列化器对结果进行序列化，并将结果发送给客户端。
6. 客户端接收到响应后，使用序列化器对响应进行反序列化，并将结果返回给用户。

The workflow of the framework is as follows:

1. The client calls a remote service via the remote proxy object.
2. The remote proxy converts the method call into an RPC request, uses a load-balancing strategy to select a service node, and sends the request to the service node.
3. After receiving the request, the service node uses a serializer to deserialize the request, finds the corresponding service implementation code, and calls the appropriate method.
4. After the service implementation is complete, the result is returned to the service node.
5. The service node uses a serializer to serialize the result and sends it to the client.
6. After receiving the response, the client uses a serializer to deserialize the response and returns the result to the user.

## 5 扩展性和可靠性 Extensibility and Reliability

我们的框架**具有良好的扩展性和可靠性**。以下是一些关键设计和决策:

+ 插件机制:我们**使用插件机制来支持多种序列化方式和负载均衡策略**。开发者可以根据自己的需求实现自定义插件，来支持其他的序列化方式或负载均衡策略。
+ 服务注册中心:使用中心化的服务注册中心来管理服务的注册和发现，提供可靠的服务节点信息。注册中心可以实现高可用以确保系统的可靠性和稳定性。
+ 异常处理:我们的框架会对**网络异常、超时等情况进行处理，提供合适的错误信息和异常处理机制，以保证框架的可靠性。**
+ 日志和监控:框架会记录关键的日志信息，方便开发者定位和解决问题，此外，我们也提供监控和性能统计功能，供开发者进行系统性能分析和优化。

Our framework **has good extensibility and reliability**. Here are some key designs and decisions:

- Plugin mechanism: We **use a plugin mechanism to support various serialization methods and load-balancing strategies**. Developers can implement custom plugins according to their needs to support other serialization methods or load-balancing strategies.
- Service registry: Uses a centralized service registry to manage the registration and discovery of services, providing reliable service node information. The registry can be highly available to ensure the reliability and stability of the system.
- Exception handling: Our framework handles **network anomalies, timeouts, etc., providing appropriate error information and exception-handling mechanisms to ensure the reliability of the framework.**
- Logging and monitoring: The framework records key log information, facilitating developers in locating and solving problems. Additionally, we provide monitoring and performance statistics functions for developers to analyze and optimize system performance.

## 6 总结 Conclusion

本文档概述了我们的RPC远程调用框架的设计和架构。通过清晰的组件划分和工作流程描述，我们的框架实现了简化分布式系统中的远程调用过程，并提供高效、可靠和易用的服务调用方式，我们相信这个框架能够满乐您的需求，并为您的分布式系统开发带来便利和效率。如果对框架设计有任何疑问或建议，请随时与我们交流。

This document outlines the design and architecture of our RPC remote calling framework. With clear component division and workflow description, our framework simplifies the process of remote calling in distributed systems and provides an efficient, reliable, and easy-to-use service calling method. We believe this framework will meet your needs and bring convenience and efficiency to your distributed system development. If you have any questions or suggestions about the framework design, please feel free to communicate with us.