package com.codingchili.core.listener;

import io.vertx.core.Future;

import com.codingchili.core.context.CoreContext;

/**
 * An unit that is deployable on the {@link CoreContext}.
 */
public interface CoreDeployment {
    /**
     * Init method that is called with the context that the service was
     * deployed from.
     *
     * @param core the core context to use.
     */
    default void init(CoreContext core) {
    }

    /**
     * @param stop complete when asynchronous shutdown is completed.
     */
    default void stop(Future<Void> stop) {
        stop.complete();
    }

    /**
     * @param start complete when asynchronous startup is completed.
     */
    default void start(Future<Void> start) {
        start.complete();
    }

}
