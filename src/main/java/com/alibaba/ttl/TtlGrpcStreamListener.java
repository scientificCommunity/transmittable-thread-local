package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlAttachments;
import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.grpc.internal.StreamListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * @author: tk
 * @since: 2021/1/20
 */
public class TtlGrpcStreamListener implements StreamListener, TtlWrapper<StreamListener>, TtlEnhanced, TtlAttachments {
    private final StreamListener listener;
    private final AtomicReference<Object> capturedRef;
    private final boolean releaseTtlValueReferenceAfterRun;
    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();

    private TtlGrpcStreamListener(@NonNull StreamListener listener, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<Object>(capture());
        this.listener = listener;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * Factory method, wrap input {@link StreamListener} to {@link TtlGrpcStreamListener}.
     *
     * @param listener input {@link StreamListener}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link StreamListener}
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already.
     */
    @Nullable
    public static TtlGrpcStreamListener get(@Nullable StreamListener listener) {
        return get(listener, false, false);
    }

    /**
     * Factory method, wrap input {@link StreamListener} to {@link TtlGrpcStreamListener}.
     *
     * @param listener                         input {@link StreamListener}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcStreamListener} is referred.
     * @return Wrapped {@link StreamListener}
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already.
     */
    @Nullable
    public static TtlGrpcStreamListener get(@Nullable StreamListener listener, boolean releaseTtlValueReferenceAfterRun) {
        return get(listener, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link StreamListener} to {@link TtlGrpcStreamListener}.
     *
     * @param listener                         input {@link StreamListener}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcStreamListener} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link StreamListener} when it's {@link TtlGrpcStreamListener},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link StreamListener}
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already and not idempotent.
     */
    @Nullable
    public static TtlGrpcStreamListener get(@Nullable StreamListener listener, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == listener) {
            return null;
        }

        if (listener instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) {
                return (TtlGrpcStreamListener) listener;
            } else {
                throw new IllegalStateException("Already TtlGrpcStreamListener!");
            }
        }
        return new TtlGrpcStreamListener(listener, releaseTtlValueReferenceAfterRun);
    }

    /**
     * wrap input {@link StreamListener} Collection to {@link TtlGrpcStreamListener} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already.
     */
    @NonNull
    public static List<TtlGrpcStreamListener> gets(@Nullable Collection<? extends StreamListener> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrap input {@link StreamListener} Collection to {@link TtlGrpcStreamListener} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcStreamListener} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already.
     */
    @NonNull
    public static List<TtlGrpcStreamListener> gets(@Nullable Collection<? extends StreamListener> tasks, boolean releaseTtlValueReferenceAfterRun) {
        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrap input {@link StreamListener} Collection to {@link TtlGrpcStreamListener} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlGrpcStreamListener} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link StreamListener} when it's {@link TtlGrpcStreamListener},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlGrpcStreamListener} already and not idempotent.
     */
    @NonNull
    public static List<TtlGrpcStreamListener> gets(@Nullable Collection<? extends StreamListener> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<TtlGrpcStreamListener> copy = new ArrayList<TtlGrpcStreamListener>();
        for (StreamListener task : tasks) {
            copy.add(TtlGrpcStreamListener.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }

    /**
     * Unwrap {@link TtlGrpcStreamListener} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
     * if input {@code Function} parameter is not a {@link TtlGrpcStreamListener} just return input {@code Function}.
     * <p>
     * so {@code TtlGrpcStreamListener.unwrap(TtlGrpcStreamListener.get(function))} will always return the same input {@code function} object.
     *
     * @see #messagesAvailable(MessageProducer)
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     * @since 2.10.2
     */
    @Nullable
    public static StreamListener unwrap(@Nullable StreamListener listener) {
        if (!(listener instanceof TtlGrpcStreamListener)) {
            return listener;
        } else {
            return ((TtlGrpcStreamListener) listener).getListener();
        }
    }

    /**
     * Unwrap {@link TtlGrpcStreamListener} to the original/underneath one for collection.
     * <p>
     * Invoke {@link #unwrap(StreamListener)} for each element in input collection.
     * <p>
     * This method is {@code null}-safe, when input {@code StreamListener} parameter collection is {@code null}, return a empty list.
     *
     * @see #gets(Collection)
     * @see #unwrap(StreamListener)
     * @since 2.10.2
     */
    @NonNull
    public static List<StreamListener> unwraps(@Nullable Collection<? extends StreamListener> tasks) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<StreamListener> copy = new ArrayList<StreamListener>();
        for (StreamListener task : tasks) {
            if (!(task instanceof TtlGrpcStreamListener)) {
                copy.add(task);
            } else {
                copy.add(((TtlGrpcStreamListener) task).getListener());
            }
        }
        return copy;
    }

    @Override
    public void messagesAvailable(MessageProducer producer) {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            listener.messagesAvailable(producer);
        } finally {
            restore(backup);
        }
    }

    @Override
    public void onReady() {
        listener.onReady();
    }

    /**
     * return original/unwrapped {@link StreamListener}.
     */
    @NonNull
    public StreamListener getListener() {
        return unwrap();
    }

    /**
     * unwrap to original/unwrapped {@link StreamListener}.
     *
     * @see TtlUnwrap#unwrap(Object)
     * @since 2.11.4
     */
    @NonNull
    @Override
    public StreamListener unwrap() {
        return listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TtlGrpcStreamListener that = (TtlGrpcStreamListener) o;

        return listener.equals(that.listener);
    }

    @Override
    public int hashCode() {
        return listener.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + listener.toString();
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
}
