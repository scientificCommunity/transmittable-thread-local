package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.spi.TtlEnhanced;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;

/**
 * @author: tk (soulmate.tangk at gmail dot com)
 * @date: 2021/2/12
 */
public class TtlWrapHandlerUtils {
    public static ChannelHandler get(@Nullable ChannelHandler handler, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == handler) {
            return null;
        }

        if (handler instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) {
                return handler;
            } else {
                throw new IllegalStateException("Already TtlNettyHandler!");
            }
        }
        /*if (handler instanceof ChannelDuplexHandler) {
            return TtlNettyDuplexHandler.get((ChannelDuplexHandler) handler, releaseTtlValueReferenceAfterRun);
        } else*/ if (handler instanceof ChannelInboundHandler) {
            return TtlNettyInboundHandler.get((ChannelInboundHandler) handler, releaseTtlValueReferenceAfterRun);
        } else {
            return handler;
        }
    }
}
