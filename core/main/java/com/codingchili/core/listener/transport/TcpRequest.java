package com.codingchili.core.listener.transport;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import com.codingchili.core.listener.*;

/**
 * @author Robin Duda
 * <p>
 * TCP request implementation.
 */
class TcpRequest implements Request {
    private Connection connection;
    private ListenerSettings settings;
    private JsonObject data;
    private int size;

    TcpRequest(Connection connection, Buffer buffer, ListenerSettings settings) {
        this.size = buffer.length();
        this.connection = connection;
        this.settings = settings;
        this.data = buffer.toJsonObject();
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void write(Object object) {
        connection.write(object);
    }

    @Override
    public JsonObject data() {
        return data;
    }

    @Override
    public int timeout() {
        return settings.getTimeout();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int maxSize() {
        return settings.getMaxRequestBytes();
    }
}
