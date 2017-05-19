package com.codingchili.core.listener.transport;

import java.util.function.Supplier;

import com.codingchili.core.context.CoreContext;
import com.codingchili.core.context.DeploymentAware;
import com.codingchili.core.files.Configurations;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.CoreListener;
import com.codingchili.core.listener.ListenerSettings;
import com.codingchili.core.listener.RequestProcessor;

import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         <p>
 *         Listens for requests addressed to the attached handler and forwards
 *         the requests to it.
 */
public class ClusterListener implements CoreListener, DeploymentAware {
    private Supplier<ListenerSettings> settings;
    private CoreHandler handler;
    private CoreContext core;

    @Override
    public void init(CoreContext core) {
        this.core = core;
    }

    @Override
    public CoreListener settings(Supplier<ListenerSettings> settings) {
        this.settings = settings;
        return this;
    }

    @Override
    public CoreListener handler(CoreHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public void start(Future<Void> start) {
        core.bus().consumer(handler.address()).handler(message -> {
            RequestProcessor.accept(core, handler, new ClusterRequest(message));
        });
        handler.start(start);
    }

    @Override
    public void stop(Future<Void> stop) {
        handler.stop(stop);
    }

    @Override
    public int instances() {
        return (handler instanceof DeploymentAware) ?
                ((DeploymentAware) handler).instances() : Configurations.system().getListeners();
    }
}
