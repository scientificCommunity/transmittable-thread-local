//package com.alibaba.ttl;
//
//import com.alibaba.ttl.spi.TtlAttachments;
//import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
//import com.alibaba.ttl.spi.TtlEnhanced;
//import com.alibaba.ttl.spi.TtlWrapper;
//import io.grpc.Metadata;
//import io.grpc.Status;
//import io.grpc.internal.ClientStreamListener;
//import io.grpc.internal.StreamListener;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;
//
///**
// * @author: tk
// * @since: 2021/1/20
// */
//public class TtlGrpcClientStreamListener implements ClientStreamListener, TtlWrapper<ClientStreamListener>, TtlEnhanced, TtlAttachments {
//    private final ClientStreamListener listener;
//    private final AtomicReference<Object> capturedRef;
//    private final boolean releaseTtlValueReferenceAfterRun;
//    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();
//
//    private TtlGrpcClientStreamListener(ClientStreamListener listener, boolean releaseTtlValueReferenceAfterRun) {
//        this.capturedRef = new AtomicReference<Object>(capture());
//        this.listener = listener;
//        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
//    }
//
//    /**
//     * Factory method, wrap input {@link ClientStreamListener} to {@link TtlGrpcClientStreamListener}.
//     *
//     * @param listener input {@link ClientStreamListener}. if input is {@code null}, return {@code null}.
//     * @return Wrapped {@link ClientStreamListener}
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already.
//     */
//
//    public static TtlGrpcClientStreamListener get(ClientStreamListener listener) {
//        return get(listener, false, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link ClientStreamListener} to {@link TtlGrpcClientStreamListener}.
//     *
//     * @param listener                         input {@link ClientStreamListener}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcClientStreamListener} is referred.
//     * @return Wrapped {@link ClientStreamListener}
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already.
//     */
//
//    public static TtlGrpcClientStreamListener get(ClientStreamListener listener, boolean releaseTtlValueReferenceAfterRun) {
//        return get(listener, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link ClientStreamListener} to {@link TtlGrpcClientStreamListener}.
//     *
//     * @param listener                         input {@link ClientStreamListener}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcClientStreamListener} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link ClientStreamListener} when it's {@link TtlGrpcClientStreamListener},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return Wrapped {@link ClientStreamListener}
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already and not idempotent.
//     */
//
//    public static TtlGrpcClientStreamListener get(ClientStreamListener listener, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == listener) {
//            return null;
//        }
//
//        if (listener instanceof TtlEnhanced) {
//            // avoid redundant decoration, and ensure idempotency
//            if (idempotent) {
//                return (TtlGrpcClientStreamListener) listener;
//            } else {
//                throw new IllegalStateException("Already TtlGrpcStreamListener!");
//            }
//        }
//        return new TtlGrpcClientStreamListener(listener, releaseTtlValueReferenceAfterRun);
//    }
//
//    /**
//     * wrap input {@link ClientStreamListener} Collection to {@link TtlGrpcClientStreamListener} Collection.
//     *
//     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already.
//     */
//    public static List<TtlGrpcClientStreamListener> gets(Collection<? extends ClientStreamListener> tasks) {
//        return gets(tasks, false, false);
//    }
//
//    /**
//     * wrap input {@link ClientStreamListener} Collection to {@link TtlGrpcClientStreamListener} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcClientStreamListener} is referred.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already.
//     */
//    public static List<TtlGrpcClientStreamListener> gets(Collection<? extends ClientStreamListener> tasks, boolean releaseTtlValueReferenceAfterRun) {
//        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * wrap input {@link ClientStreamListener} Collection to {@link TtlGrpcClientStreamListener} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcClientStreamListener} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link ClientStreamListener} when it's {@link TtlGrpcClientStreamListener},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlGrpcClientStreamListener} already and not idempotent.
//     */
//
//    public static List<TtlGrpcClientStreamListener> gets(Collection<? extends ClientStreamListener> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<TtlGrpcClientStreamListener> copy = new ArrayList<TtlGrpcClientStreamListener>();
//        for (ClientStreamListener task : tasks) {
//            copy.add(TtlGrpcClientStreamListener.get(task, releaseTtlValueReferenceAfterRun, idempotent));
//        }
//        return copy;
//    }
//
//    /**
//     * Unwrap {@link TtlGrpcClientStreamListener} to the original/underneath one.
//     * <p>
//     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
//     * if input {@code Function} parameter is not a {@link TtlGrpcClientStreamListener} just return input {@code Function}.
//     * <p>
//     * so {@code TtlGrpcStreamListener.unwrap(TtlGrpcStreamListener.get(function))} will always return the same input {@code function} object.
//     *
//     * @see #messagesAvailable(StreamListener.MessageProducer)
//     * @see TtlUnwrap#unwrap(Object)
//     * @since 2.10.2
//     */
//
//    public static ClientStreamListener unwrap(ClientStreamListener listener) {
//        if (!(listener instanceof TtlGrpcClientStreamListener)) {
//            return listener;
//        } else {
//            return ((TtlGrpcClientStreamListener) listener).getListener();
//        }
//    }
//
//    /**
//     * Unwrap {@link TtlGrpcClientStreamListener} to the original/underneath one for collection.
//     * <p>
//     * Invoke {@link #unwrap(ClientStreamListener)} for each element in input collection.
//     * <p>
//     * This method is {@code null}-safe, when input {@code ClientStreamListener} parameter collection is {@code null}, return a empty list.
//     *
//     * @see #gets(Collection)
//     * @see #unwrap(ClientStreamListener)
//     * @since 2.10.2
//     */
//
//    public static List<ClientStreamListener> unwraps(Collection<? extends ClientStreamListener> tasks) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<ClientStreamListener> copy = new ArrayList<ClientStreamListener>();
//        for (ClientStreamListener task : tasks) {
//            if (!(task instanceof TtlGrpcClientStreamListener)) {
//                copy.add(task);
//            } else {
//                copy.add(((TtlGrpcClientStreamListener) task).getListener());
//            }
//        }
//        return copy;
//    }
//
//    @Override
//    public void messagesAvailable(StreamListener.MessageProducer producer) {
//        final Object captured = capturedRef.get();
//        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
//            throw new IllegalStateException("TTL value reference is released after run!");
//        }
//
//        final Object backup = replay(captured);
//        try {
//            listener.messagesAvailable(producer);
//        } finally {
//            restore(backup);
//        }
//    }
//
//    @Override
//    public void onReady() {
//        listener.onReady();
//    }
//
//    /**
//     * return original/unwrapped {@link ClientStreamListener}.
//     */
//
//    public ClientStreamListener getListener() {
//        return unwrap();
//    }
//
//    /**
//     * unwrap to original/unwrapped {@link ClientStreamListener}.
//     *
//     * @see TtlUnwrap#unwrap(Object)
//     * @since 2.11.4
//     */
//
//    @Override
//    public ClientStreamListener unwrap() {
//        return listener;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        TtlGrpcClientStreamListener that = (TtlGrpcClientStreamListener) o;
//
//        return listener.equals(that.listener);
//    }
//
//    @Override
//    public int hashCode() {
//        return listener.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getName() + " - " + listener.toString();
//    }
//
//    /**
//     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
//     *
//     * @since 2.11.0
//     */
//    @Override
//    public void setTtlAttachment(String key, Object value) {
//        ttlAttachment.setTtlAttachment(key, value);
//    }
//
//    /**
//     * see {@link TtlAttachments#getTtlAttachment(String)}
//     *
//     * @since 2.11.0
//     */
//    @Override
//    public <T> T getTtlAttachment(String key) {
//        return ttlAttachment.getTtlAttachment(key);
//    }
//
//    @Override
//    public void headersRead(Metadata headers) {
//        this.listener.headersRead(headers);
//    }
//
//    @Override
//    public void closed(Status status, Metadata trailers) {
//        this.listener.closed(status, trailers);
//    }
//
//    @Override
//    public void closed(Status status, RpcProgress rpcProgress, Metadata trailers) {
//        this.listener.closed(status, rpcProgress, trailers);
//    }
//}
