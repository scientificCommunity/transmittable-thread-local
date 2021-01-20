package io.grpc.internal;

import java.io.InputStream;

/**
 * An observer of {@link io.grpc.internal.Stream} events. It is guaranteed to only have one concurrent callback at a
 * time.
 *
 * @author: tk
 * @since: 2021/1/20
 */
public interface StreamListener {

    /**
     * Called upon receiving a message from the remote end-point.
     *
     * <p>Implementations must eventually drain the provided {@code producer} {@link MessageProducer}
     * completely by invoking {@link MessageProducer#next()} to obtain deframed messages until the
     * producer returns null.
     *
     * <p>This method should return quickly, as the same thread may be used to process other streams.
     *
     * @param producer supplier of deframed messages.
     */
    void messagesAvailable(MessageProducer producer);

    /**
     * This indicates that the transport is now capable of sending additional messages
     * without requiring excessive buffering internally. This event is
     * just a suggestion and the application is free to ignore it, however doing so may
     * result in excessive buffering within the transport.
     */
    void onReady();

    /**
     * A producer for deframed gRPC messages.
     */
    interface MessageProducer {
        /**
         * Returns the next gRPC message, if the data has been received by the deframer and the
         * application has requested another message.
         *
         * <p>The provided {@code message} {@link InputStream} must be closed by the listener.
         *
         * <p>This is intended to be used similar to an iterator, invoking {@code next()} to obtain
         * messages until the producer returns null, at which point the producer may be discarded.
         */
        public InputStream next();
    }
}
