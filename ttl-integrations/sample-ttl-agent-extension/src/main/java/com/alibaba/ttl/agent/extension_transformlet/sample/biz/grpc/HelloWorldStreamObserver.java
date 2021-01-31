package com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc;

import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

/**
 * @author: tk
 * @since: 2021/1/31
 */
public class HelloWorldStreamObserver implements StreamObserver<HelloReply> {
    private static final Logger logger = Logger.getLogger(HelloWorldStreamObserver.class.getName());

    @Override
    public void onNext(HelloReply s) {
        logger.info("======================="+ContextUtils.getThreadLocal()+"=======================");
        logger.info("+++++++++++++++++++++++"+ContextUtils.getTtlThreadLocal()+"+++++++++++++++++++++++");
        logger.info("Greeting: " + s);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        System.out.println(222);
    }
}
