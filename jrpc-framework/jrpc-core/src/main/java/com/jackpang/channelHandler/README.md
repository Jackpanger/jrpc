
1. Server Consumer

    Send Packet writeAndFlush(object)    Request
    
    What information should be included: 
    
    JrpcRequest :   
    
    	1. Request id (long)
    	1. Compression type (1byte)
    	1. Serialization method (1byte)
    	1. Message type (normal request, heartbeat detection request) (1byte)
    	1. Payload (interface name, method name, parameter list, return type))
    
    pipeline valid -> packet outBound
    ---> first handler in/out log
    ---> second handler encoder(out)(convert object to msg packet, serialization, compression)
2. Server Provider

    receive Packet by Netty
    pipeline valid -> packet inBound
    ---> first handler in/out log
    ---> second handler decoder (in) (decompression,deserialization, convert msg packet to jrpcRequest)
    ---> third handler(in) jrpcRequest -> jrpcResponse
3. Call the function to get the result
4. Server Provider

   Send Packet writeAndFlush(object) response
   pipeline valid -> packet outBound
   ---> first handler(out)(convert object to msg packet)
   ---> second handler(out)(serialization)
   ---> third handler(out)(compression)
5. Server Consumer

   receive Packet by Netty
   pipeline valid -> packet inBound
   ---> first handler(in)(decompression)
   ---> second handler(in)(deserialization)
   ---> third handler(in)(convert msg packet to object)
6. Get results and return