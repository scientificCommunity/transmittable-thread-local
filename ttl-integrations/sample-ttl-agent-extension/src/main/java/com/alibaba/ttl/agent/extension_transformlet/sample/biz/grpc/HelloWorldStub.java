package com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;

/**
 * @author: tk
 * @since: 2021/1/31
 */
public class HelloWorldStub extends AbstractStub<HelloWorldStub> {
    protected HelloWorldStub(Channel channel) {
        super(channel);
    }

    protected HelloWorldStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected HelloWorldStub build(Channel channel, CallOptions callOptions) {
        return new HelloWorldStub(channel, callOptions);
    }

    /*public <T> void helloWorld(String request,
                           StreamObserver<T> observer) {
        asyncUnaryCall(
            getChannel().newCall(getCallOptions()), request, observer);
    }*/
}
