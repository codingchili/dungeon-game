package com.codingchili.realmregistry.controller;

import com.codingchili.realmregistry.ContextMock;
import com.codingchili.realmregistry.configuration.RealmSettings;
import com.codingchili.realmregistry.configuration.RegistryContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;
import com.codingchili.core.security.Token;
import com.codingchili.core.security.TokenFactory;
import com.codingchili.core.testing.*;

import static com.codingchili.common.Strings.*;


/**
 * @author Robin Duda
 *         tests the API from realmName->authentication server.
 */

@RunWith(VertxUnitRunner.class)
public class ServerHandlerTest {
    private static final String REALM_NAME = "test-realm";
    private RealmSettings realmconfig = new RealmSettings();
    private RealmHandler<RegistryContext> handler;
    private ContextMock mock;

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Before
    public void setUp() {
        mock = new ContextMock(Vertx.vertx());
        handler = new RealmHandler<>(mock);

        realmconfig.setAuthentication(new Token(mock.getRealmFactory(), REALM_NAME));
        realmconfig.setName(REALM_NAME);
    }

    @After
    public void tearDown(TestContext test) {
        mock.vertx().close(test.asyncAssertSuccess());
    }

    @Test
    public void failRegisterRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void failWithClientToken(TestContext test) {
        Token token = new Token(mock.getClientFactory(), realmconfig.getName());
        realmconfig.setAuthentication(token);

        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });

        realmconfig = new RealmSettings();
    }

    @Test
    public void updateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, getToken()));
    }

    @Test
    public void failUpdateRealmTest(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testClientClose(TestContext test) {
        // need to register realm before removing
        updateRealmTest(test);

        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.ACCEPTED, status);
        }, new JsonObject()
                .put(ID_REALM, Serializer.json(realmconfig))
                .put(ID_TOKEN, Serializer.json(realmconfig.getAuthentication())));
    }

    @Test
    public void failClientCloseMissingRealm(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.ERROR, status);
        }, new JsonObject()
                .put(ID_TOKEN, getToken()));
    }

    @Test
    public void failRealmClose(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(ResponseStatus.UNAUTHORIZED, status);
        });
    }

    @Test
    public void testPingAuthenticationHandler(TestContext test) {
        handle(ID_PING, (response, status) -> {
            test.assertEquals(status, ResponseStatus.ACCEPTED);
        });
    }

    @Test
    public void failUpdateWhenInvalidToken(TestContext test) {
        handle(REALM_UPDATE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    @Test
    public void failCloseWhenInvalidToken(TestContext test) {
        handle(CLIENT_CLOSE, (response, status) -> {
            test.assertEquals(status, ResponseStatus.UNAUTHORIZED);
        });
    }

    private void handle(String action, ResponseListener listener) {
        handle(action, listener, null);
    }

    private void handle(String action, ResponseListener listener, JsonObject data) {
        handler.process(RequestMock.get(action, listener, data));
    }

    private JsonObject getToken() {
        return Serializer.json(new Token(mock.getRealmFactory(), REALM_NAME));
    }
}
