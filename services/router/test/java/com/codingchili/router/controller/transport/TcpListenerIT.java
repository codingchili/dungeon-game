package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * @author Robin Duda
 *
 * Test cases for UDP transport.
 */
@RunWith(VertxUnitRunner.class)
public class TcpListenerIT extends TransportTestCases {

    public TcpListenerIT() {
        super(WireType.TCP);
    }

    @Override
    void sendRequest(ResponseListener listener, JsonObject data) {
        vertx.createNetClient().connect(port, HOST, connect -> {
            if (connect.succeeded()) {
                NetSocket socket = connect.result();
                socket.handler(buffer -> handleBody(listener, buffer));
                socket.write(data.encode());
            } else {
                throw new RuntimeException(connect.cause());
            }
        });
    }
}