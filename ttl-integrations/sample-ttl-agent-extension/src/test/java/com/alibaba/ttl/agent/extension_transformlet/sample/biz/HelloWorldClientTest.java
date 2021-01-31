/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.ttl.agent.extension_transformlet.sample.biz;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc.*;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link HelloWorldClientBlocking}.
 * For demonstrating how to write gRPC unit test only.
 * Not intended to provide a high code coverage or to test every major usecase.
 * <p>
 * directExecutor() makes it easier to have deterministic tests.
 * However, if your implementation uses another thread and uses streaming it is better to use
 * the default executor, to avoid hitting bug #3084.
 */
@RunWith(JUnit4.class)
public class HelloWorldClientTest {
    /**
     * This rule manages automatic graceful shutdown for the registered servers and channels at the
     * end of test.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final GreeterGrpc.GreeterImplBase serviceImpl =
        mock(GreeterGrpc.GreeterImplBase.class, delegatesTo(
            new GreeterGrpc.GreeterImplBase() {
                // By default the client will receive Status.UNIMPLEMENTED for all RPCs.
                // You might need to implement necessary behaviors for your test here, like this:
                //
                @Override
                public void sayHello(HelloRequest request, StreamObserver<HelloReply> respObserver) {
                    respObserver.onNext(HelloReply.getDefaultInstance());
                    respObserver.onCompleted();
                    System.out.println("======================="+ContextUtils.getThreadLocal()+"=======================");
                    System.out.println("+++++++++++++++++++++++"+ContextUtils.getTtlThreadLocal()+"+++++++++++++++++++++++");
                    System.out.println(respObserver.getClass());
                }
            }));

    private HelloWorldClientStub client;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
            .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
            InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
        client = new HelloWorldClientStub(channel);
    }

    /**
     * To test the client, call from the client against the fake server, and verify behaviors or state
     * changes from the server side.
     */
    @Test
    public void greet_messageDeliveredToServer() {
        ArgumentCaptor<HelloRequest> requestCaptor = ArgumentCaptor.forClass(HelloRequest.class);

        client.greet("test name");

        ContextUtils.set("hahahahhaha");

        if (TtlAgent.isTtlAgentLoaded()) {
            System.out.println("Test WITH TTL Agent");
        } else {
            System.out.println("Test Without TTL Agent");
        }
        verify(serviceImpl)
            .sayHello(requestCaptor.capture(), ArgumentMatchers.<StreamObserver<HelloReply>>any());
        Assert.assertEquals("test name", requestCaptor.getValue().getName());
    }

    @Test
    public void test1() throws InterruptedException {
        String user = "world";
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build();
        try {
            HelloWorldClientStub client = new HelloWorldClientStub(channel);
            client.greet(user);
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
