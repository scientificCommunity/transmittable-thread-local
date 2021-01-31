package com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc;

import com.google.protobuf.MessageLite;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.lite.ProtoLiteUtils;

/**
 * @author: tk
 * @since: 2021/1/31
 */
public final class HelloWorldStubFactory {
    public static HelloWorldStub createStub(Channel channel) {
        return new HelloWorldStub(channel);
    }

    /*public static MethodDescriptor<String, String> getMethodDescriptor() {
        return MethodDescriptor.newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc.HelloWorldStub/helloWorld")
            .setSampledToLocalTracing(true)
            .setRequestMarshaller(ProtoLiteUtils.marshaller(MessageLite))
    }*/
}
