package com.codingchili.services.Logging;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.Context.CoreContext;
import com.codingchili.core.Protocol.ResponseStatus;
import com.codingchili.core.Protocol.Serializer;
import com.codingchili.core.Security.Token;
import com.codingchili.core.Security.TokenFactory;
import com.codingchili.core.Testing.RequestMock;
import com.codingchili.core.Testing.ResponseListener;

import com.codingchili.services.Logging.Configuration.LogContext;
import com.codingchili.services.Logging.Configuration.LogServerSettings;
import com.codingchili.services.Logging.Controller.ClientLogHandler;
import com.codingchili.services.Logging.Controller.ServiceLogHandler;
import com.codingchili.services.Shared.Strings;

import static com.codingchili.services.Shared.Strings.ID_TOKEN;

/**
 * @author Robin Duda
 */
@RunWith(VertxUnitRunner.class)
public class ClientLogHandlerTest {
    private TokenFactory factory;
    private ClientLogHandler handler;
    private LogContext context;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        LogServerSettings settings = new LogServerSettings();
        settings.setSecret(new byte[]{0x0});

        context = new ContextMock(settings, Vertx.vertx());
        factory = new TokenFactory(settings.getSecret());
        handler = new ClientLogHandler<>(context);
    }
    
    @After
    public void tearDown(TestContext test) {
        context.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void logMessage(TestContext test) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, getToken());
    }

    @Test
    public void failLogMessageWhenInvalidToken(TestContext test) {
        handle(Strings.PROTOCOL_LOGGING, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    private JsonObject getToken() {
        return new JsonObject().put(ID_TOKEN, Serializer.json(new Token(factory, "domain")));
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }
}
