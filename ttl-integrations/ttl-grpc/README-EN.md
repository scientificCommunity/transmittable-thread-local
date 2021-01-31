## Grpc integration of TTL
The purpose of this integration is to ensure that<br/> 
we can get the preset `TTL context` correctly in the callbacks of asynchronous calls from the `Grpc client`
### 1.1 修饰`io.grpc.internal.ClientStreamListener`

Use [`TtlGrpcClientStreamListener`](src/main/java/com/alibaba/ttl/integration/grpc/TtlGrpcClientStreamListener.java) to decorate `Listener`。

### 1.2 Modify the executor class of listener

At present, `TTL` agent has decorated below `Grpc Async Call` callback components(`io.grpc.internal.ClientStreamListener`) implementation:

- `io.grpc.internal.AbstractClientStream`
- `io.grpc.internal.DelayedStream`
---
- decoration implementation code is in [`GrpcClientStreamTransformlet.java`](src/main/java/com/alibaba/ttl/integration/grpc/agent/transformlet/GrpcClientStreamTransformlet.java)。
    
