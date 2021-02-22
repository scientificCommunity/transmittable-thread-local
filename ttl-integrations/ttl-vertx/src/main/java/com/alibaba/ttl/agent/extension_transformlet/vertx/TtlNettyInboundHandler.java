package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.TtlUnwrap;
import com.alibaba.ttl.spi.TtlAttachments;
import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpClientCodec;
import io.vertx.core.Handler;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * @author: tk (soulmate.tangk at gmail dot com)
 * @date: 2021/2/11
 */
public class TtlNettyInboundHandler implements ChannelInboundHandler, TtlWrapper<ChannelInboundHandler>, TtlEnhanced, TtlAttachments {
    private final AtomicReference<Object> capturedRef;
    private final ChannelInboundHandler handler;
    private final boolean releaseTtlValueReferenceAfterRun;
    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();

    private TtlNettyInboundHandler(@NonNull ChannelInboundHandler handler, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<Object>(capture());
        this.handler = handler;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * Factory method, wrap input {@link ChannelInboundHandler} to {@link TtlNettyInboundHandler}.
     *
     * @param handler input {@link ChannelInboundHandler}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link ChannelInboundHandler}
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already.
     */
    @Nullable
    public static ChannelHandler get(@Nullable ChannelInboundHandler handler) {
        return get(handler, false, false);
    }

    /**
     * Factory method, wrap input {@link Handler} to {@link TtlNettyInboundHandler}.
     *
     * @param handler                          input {@link Handler}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlNettyInboundHandler} is referred.
     * @return Wrapped {@link Handler}
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already.
     */
    @Nullable
    public static ChannelHandler get(@Nullable ChannelInboundHandler handler, boolean releaseTtlValueReferenceAfterRun) {
        return get(handler, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link ChannelInboundHandler} to {@link TtlNettyInboundHandler}.
     *
     * @param handler                          input {@link ChannelInboundHandler}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlNettyInboundHandler} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link ChannelInboundHandler} when it's {@link TtlNettyInboundHandler},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link ChannelInboundHandler}
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already and not idempotent.
     */
    @Nullable
    public static ChannelHandler get(@Nullable ChannelHandler handler, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == handler) {
            return null;
        }

        if (handler instanceof HttpClientCodec) {
            return handler;
        }
        if (handler instanceof ChannelInboundHandler) {

            if (handler instanceof TtlEnhanced) {
                // avoid redundant decoration, and ensure idempotency
                if (idempotent) {
                    return handler;
                } else {
                    throw new IllegalStateException("Already TtlNettyHandler!");
                }
            }
            return new TtlNettyInboundHandler((ChannelInboundHandler) handler, releaseTtlValueReferenceAfterRun);
        } else {
            return handler;
        }
    }

    /**
     * wrap input {@link ChannelInboundHandler} Collection to {@link TtlNettyInboundHandler} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already.
     */
    @NonNull
    public static List<ChannelHandler> gets(@Nullable Collection<? extends ChannelInboundHandler> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrap input {@link ChannelInboundHandler} Collection to {@link TtlNettyInboundHandler} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlNettyInboundHandler} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already.
     */
    @NonNull
    public static List<ChannelHandler> gets(@Nullable Collection<? extends ChannelInboundHandler> tasks, boolean releaseTtlValueReferenceAfterRun) {
        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrap input {@link ChannelInboundHandler} Collection to {@link TtlNettyInboundHandler} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlNettyInboundHandler} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link ChannelInboundHandler} when it's {@link TtlNettyInboundHandler},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlNettyInboundHandler} already and not idempotent.
     */
    @NonNull
    public static List<ChannelHandler> gets(@Nullable Collection<? extends ChannelInboundHandler> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<ChannelHandler> copy = new ArrayList<ChannelHandler>();
        for (ChannelHandler task : tasks) {
            copy.add(TtlNettyInboundHandler.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }

    /**
     * Unwrap {@link TtlNettyInboundHandler} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
     * if input {@code Function} parameter is not a {@link TtlNettyInboundHandler} just return input {@code Function}.
     * <p>
     * so {@code TtlNettyHandler.unwrap(TtlNettyHandler.get(function))} will always return the same input {@code function} object.
     *
     * @see TtlUnwrap#unwrap(Object)
     * @since 2.10.2
     */
    @Nullable
    public static ChannelInboundHandler unwrap(@Nullable ChannelInboundHandler handler) {
        if (!(handler instanceof TtlNettyInboundHandler)) {
            return handler;
        } else {
            return ((TtlNettyInboundHandler) handler).getHandler();
        }
    }

    /**
     * Unwrap {@link TtlNettyInboundHandler} to the original/underneath one for collection.
     * <p>
     * Invoke {@link #unwrap(ChannelInboundHandler)} for each element in input collection.
     * <p>
     * This method is {@code null}-safe, when input {@code ChannelInboundHandler} parameter collection is {@code null}, return a empty list.
     *
     * @see #gets(Collection)
     * @see #unwrap(ChannelInboundHandler)
     * @since 2.10.2
     */
    @NonNull
    public static List<ChannelInboundHandler> unwraps(@Nullable Collection<? extends ChannelInboundHandler> tasks) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<ChannelInboundHandler> copy = new ArrayList<ChannelInboundHandler>();
        for (ChannelInboundHandler task : tasks) {
            if (!(task instanceof TtlNettyInboundHandler)) {
                copy.add(task);
            } else {
                copy.add(((TtlNettyInboundHandler) task).getHandler());
            }
        }
        return copy;
    }

    @NonNull
    public ChannelInboundHandler getHandler() {
        return unwrap();
    }

    /**
     * unwrap to original/unwrapped {@link ChannelInboundHandler}.
     *
     * @see TtlUnwrap#unwrap(Object)
     * @since 2.11.4
     */
    @NonNull
    @Override
    public ChannelInboundHandler unwrap() {
        return handler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TtlNettyInboundHandler that = (TtlNettyInboundHandler) o;

        return handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        return handler.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + handler.toString();
    }

    /**
     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
     *
     * @since 2.11.0
     */
    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        ttlAttachment.setTtlAttachment(key, value);
    }

    /**
     * see {@link TtlAttachments#getTtlAttachment(String)}
     *
     * @since 2.11.0
     */
    @Override
    public <T> T getTtlAttachment(@NonNull String key) {
        return ttlAttachment.getTtlAttachment(key);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelRegistered(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelUnregistered(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelUnregistered(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelInactive(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelRead(channelHandlerContext, o);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelReadComplete(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.userEventTriggered(channelHandlerContext, o);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.channelWritabilityChanged(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.handlerAdded(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.handlerRemoved(channelHandlerContext);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.exceptionCaught(channelHandlerContext, throwable);
        } finally {
            restore(backup);
        }
    }
}
