package com.codingchili.core.protocol;

import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;
import io.vertx.core.json.JsonObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Maps packet data to handlers and manages authentication status for handlers.
 */
public class Protocol<Handler extends RequestHandler> {
    private final AuthorizationHandler<Handler> handlers = new AuthorizationHandler<>();

    public Protocol() {}

    /**
     * Creates a protocol by mapping @Public and @Private annotated methods.
     *
     * @param handler contains methods to be mapped.
     */
    public Protocol(CoreHandler handler) {
        Method[] methods = handler.getClass().getDeclaredMethods();

        for (Method method : methods) {
            Annotation annotation = method.getAnnotation(Public.class);

            if (annotation != null) {
                use(((Public) annotation).value(), wrap(handler, method), Access.PUBLIC);
            } else {
                annotation = method.getAnnotation(Private.class);
                if (annotation != null) {
                    use(((Private) annotation).value(), wrap(handler, method), Access.AUTHORIZED);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Handler wrap(CoreHandler handler, Method method) {
        return (Handler) new RequestHandler<Request>() {
            @Override
            public void handle(Request request) {
                try {
                    method.invoke(handler, request);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param access the access level the request is valid for.
     * @param route  the handler route to find
     * @return the handler that is mapped to the route and access level.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public Handler get(Access access, String route) throws AuthorizationRequiredException, HandlerMissingException {
        if (handlers.contains(route)) {
            return handlers.get(route, access);
        } else {
            return handlers.get(ANY, access);
        }
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param route the handler route to find.
     * @return the handler that is mapped to the route.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for the given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public Handler get(String route) throws AuthorizationRequiredException, HandlerMissingException {
        return get(Access.AUTHORIZED, route);
    }

    /**
     * Registers a handler for the given route.
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<Handler> use(String route, Handler handler) {
        use(route, handler, Access.AUTHORIZED);
        return this;
    }

    /**
     * Registers a handler for the given route with an access level.
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route with the access level.
     * @param access  specifies the authorization level required to access the route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<Handler> use(String route, Handler handler, Access access) {
        handlers.use(route, handler, access);
        return this;
    }

    /**
     * @return returns a list of all registered routes on the protoocol.
     */
    public ProtocolMapping list() {
        return handlers.list();
    }

    /**
     * Creates a response object given a response status.
     *
     * @param status the status to create the response from.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject response(ResponseStatus status) {
        return new JsonObject()
                .put(PROTOCOL_STATUS, status);
    }

    /**
     * Creates a response object given a response status and a throwable.
     *
     * @param status the status to include in the response.
     * @param e      an exception that was the cause of an abnormal response status.
     * @return a JSON encoded response packed in a buffer.
     */
    public static JsonObject response(ResponseStatus status, Throwable e) {
        return new JsonObject()
                .put(PROTOCOL_STATUS, status)
                .put(PROTOCOL_MESSAGE, e.getMessage());
    }
}

