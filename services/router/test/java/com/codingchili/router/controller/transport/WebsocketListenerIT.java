package com.codingchili.router.controller.transport;

import com.codingchili.router.model.WireType;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.runner.RunWith;

import static com.codingchili.common.Strings.DIR_SEPARATOR;

/**
 * @author Robin Duda
 *         <p>
 *         Test cases for HTTP/REST transport.
 */
@RunWith(VertxUnitRunner.class)
public class WebsocketListenerIT extends TransportTestCases {

    public WebsocketListenerIT() {
        super(WireType.WEBSOCKET);
    }

    @Override
    void sendRequest(ResponseListener listener, JsonObject data) {
        vertx.createHttpClient().websocket(port, HOST, DIR_SEPARATOR, handler -> {
            handler.handler(body -> handleBody(listener, body));
            handler.write(Buffer.buffer(data.encode()));
        });
    }
}