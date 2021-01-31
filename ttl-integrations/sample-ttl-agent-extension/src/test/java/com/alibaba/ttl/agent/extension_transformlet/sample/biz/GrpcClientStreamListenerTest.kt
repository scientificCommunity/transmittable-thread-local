package com.alibaba.ttl.agent.extension_transformlet.sample.biz

import com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc.HelloWorldStub
import com.alibaba.ttl.agent.extension_transformlet.sample.biz.grpc.HelloWorldStubFactory
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import org.junit.Test

/**
 * @author: tk
 * @since: 2021/1/31
 */
class GrpcClientStreamListenerTest {
    private var server: Server? = null

    @Test
    fun testTtlInVertx() {

    }

    private fun initClient() {
        val target = "host:port"
        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        val channel = ManagedChannelBuilder.forTarget(target) // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build()

        val stub = HelloWorldStubFactory.createStub(channel)
    }
}
