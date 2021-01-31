## Grpc 的TTL集成
该集成是为了保证在`Grpc client`的**异步调用的回调中**能够正确拿到预设好的`ttl context`<br/>
目前支持的`Grpc`版本为`1.28.1`
### 1.1 修饰`io.grpc.internal.ClientStreamListener`

使用[`TtlGrpcClientStreamListener`](src/main/java/com/alibaba/ttl/integration/grpc/TtlGrpcClientStreamListener.java)来修饰传入的`Listener`。

### 1.2 修改执行器类

修饰了的Grpc执行器组件如下:
- `io.grpc.internal.AbstractClientStream`
- `io.grpc.internal.DelayedStream`
---
- 修饰实现代码在[`GrpcClientStreamTransformlet.java`](src/main/java/com/alibaba/ttl/integration/grpc/agent/transformlet/GrpcClientStreamTransformlet.java)。
    
