package com.codingchili.core.protocol;

import com.esotericsoftware.reflectasm.MethodAccess;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingchili.core.configuration.CoreStrings;
import com.codingchili.core.listener.Receiver;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.exception.AuthorizationRequiredException;
import com.codingchili.core.protocol.exception.HandlerMissingException;

import static com.codingchili.core.configuration.CoreStrings.*;
import static com.codingchili.core.protocol.RoleMap.*;

/**
 * @author Robin Duda
 * <p>
 * Maps packet data to authorizer and manages authentication status for authorizer.
 * <p>
 * Route documentation and listing may be retrieved using #{@link #getDescription()}
 * or by calling the protocol with the #{@link CoreStrings#PROTOCOL_DOCUMENTATION}
 * route. The documentation route is enabled whenever a handler class or route
 * is documented using either #{@link #document(String)}, #{@link #setDescription(String)}
 * or by adding the #{@link Description} annotation to the class or handler method.
 */
public class Protocol<RequestType> {
    private AuthorizationHandler<RequestType> authorizer = new SimpleAuthorizationHandler<>();
    private String description = CoreStrings.getDescriptionMissing();
    private RoleType[] defaultRoles = new RoleType[]{RoleMap.get(USER)};
    private boolean emitDocumentation = false;
    private Route<RequestType> lastAddedRoute;
    private Class<?> dataModel;

    public Protocol() {
    }

    /**
     * Creates a protocol by mapping annotated methods.
     *
     * @param handler contains methods to be mapped.
     */
    public Protocol(Receiver<RequestType> handler) {
        annotated(handler);
    }

    /**
     * Processes annotations on the given handler.
     *
     * @param handler the handler that is annotated
     * @return fluent
     */
    public Protocol<RequestType> annotated(Receiver<RequestType> handler) {
        setHandlerProperties(handler.getClass());
        setHandlerRoutes(handler);
        return this;
    }

    /**
     * Replaces the authorization handler used to map roles to routes.
     *
     * @param authorizer the new authorizer to use
     * @return fluent
     */
    public Protocol<RequestType> setAuthorizationHandler(AuthorizationHandler<RequestType> authorizer) {
        this.authorizer.list().forEach(authorizer::use);
        this.authorizer = authorizer;
        return this;
    }

    private void setHandlerProperties(Class<?> handlerClass) {
        Roles classRole = handlerClass.getAnnotation(Roles.class);
        Description classDescription = handlerClass.getAnnotation(Description.class);
        DataModel classModel = handlerClass.getAnnotation(DataModel.class);

        if (classDescription != null) {
            this.emitDocumentation = true;
            this.description = classDescription.value();
        }
        if (classRole != null) {
            this.defaultRoles = RoleMap.get(classRole.value());
        }
        if (classModel != null) {
            setDataModel(classModel.value());
        }
    }

    private void setHandlerRoutes(Receiver<RequestType> handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            Api api = method.getAnnotation(Api.class);
            Description description = method.getAnnotation(Description.class);
            DataModel model = method.getAnnotation(DataModel.class);

            if (api != null) {
                String route = (api.route().isEmpty()) ? method.getName() : api.route();
                RoleType[] roles = (api.value()[0].equals(UNSET)) ? defaultRoles : RoleMap.get(api.value());

                wrap(route, handler, method, roles);

                if (description != null) {
                    emitDocumentation = true;
                    this.document(description.value());
                }

                if (model != null) {
                    this.model(model.value());
                }
            }
        }
    }

    /**
     * Sets the data model used for requests for documentation purposes.
     * Set automatically when using #{@link #annotated(Receiver)}
     *
     * @param model the data transfer object.
     * @return fluent
     */
    public Protocol<RequestType> setDataModel(Class<?> model) {
        this.dataModel = model;
        return this;
    }

    /**
     * Sets the default role of routes added _after_ this call.
     * <p>
     * Annotated alternative #{@link Roles} on handler class
     *
     * @param role the role to set for requests added after calling this method.
     * @return fluent
     */
    public Protocol<RequestType> setRole(RoleType... role) {
        this.defaultRoles = role;
        return this;
    }

    private void wrap(String route, Receiver<RequestType> handler, Method method, RoleType[] role) {
        MethodAccess access = MethodAccess.get(handler.getClass());

        int index = access.getIndex(method.getName());
        use(route, request -> {
            try {
                access.invoke(handler, index, request);
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }, role);
    }

    /**
     * Registers a handler for the given route.
     * <p>
     * Annotated alternative #{@link Api}
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<RequestType> use(String route, RequestHandler<RequestType> handler) {
        use(route, handler, defaultRoles);
        return this;
    }

    /**
     * Registers a handler for the given route with an access level.
     * <p>
     * Annotated alternative #{@link Api}
     *
     * @param route   the route to register a handler for.
     * @param handler the handler to be registered for the given route with the access level.
     * @param role    specifies the authorization level required to access the route.
     * @return the updated protocol specification for fluent use.
     */
    public Protocol<RequestType> use(String route, RequestHandler<RequestType> handler, RoleType... role) {
        lastAddedRoute = new Route<>(route, handler, role);
        authorizer.use(lastAddedRoute);
        return this;
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param route the handler route to find.
     * @return the handler that is mapped to the route.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for the given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public RequestHandler<RequestType> get(String route) throws AuthorizationRequiredException, HandlerMissingException {
        return get(route, Role.PUBLIC);
    }

    /**
     * Returns the route handler for the given target route and its access level.
     *
     * @param route the handler route to find
     * @param role  list of roles that are allowed to map to a route
     * @return the handler that is mapped to the route and access level.
     * @throws AuthorizationRequiredException when authorization level is not fulfilled for given route.
     * @throws HandlerMissingException        when the requested route handler is not registered.
     */
    public RequestHandler<RequestType> get(String route, RoleType role) throws AuthorizationRequiredException, HandlerMissingException {
        if (authorizer.contains(route)) {
            return authorizer.get(route, role);
        } else if (authorizer.contains(ANY)) {
            return authorizer.get(ANY, role);   // fallback to any route.
        } else {
            // no route registered, check if protocol emits documentation.
            if (emitDocumentation && route.equals(CoreStrings.PROTOCOL_DOCUMENTATION)) {
                return request -> {
                    if (request instanceof Request) {
                        ((Request) request).write(getDescription());
                    }
                };
            } else {
                throw new HandlerMissingException(route);
            }
        }
    }

    /**
     * Adds a documentation string to the last added route.
     * <p>
     * Annotated alternative #{@link Description} on requesthandler class
     *
     * @param routeDescription route protocol description text
     * @return fluent
     */
    public Protocol<RequestType> document(String routeDescription) {
        Objects.requireNonNull(lastAddedRoute, CoreStrings.cannotDocumentBeforeUse());
        lastAddedRoute.setDescription(routeDescription);
        return this;
    }

    /**
     * Sets the model of the last added route.
     *
     * @param model the model to set for the route.
     * @return fluent
     */
    public Protocol<RequestType> model(Class<?> model) {
        Objects.requireNonNull(lastAddedRoute, CoreStrings.cannotSetModelBeforeUse());
        lastAddedRoute.setModel(model);
        return this;
    }

    /**
     * @return returns a list of all registered routes on the protoocol.
     */
    public ProtocolDescription<RequestType> getDescription() {
        return new ProtocolDescription<>(dataModel, authorizer.list(), description);
    }

    /**
     * Sets the documentation string for this protocol suite.
     * <p>
     * Annotated alternative #{@link Description} on Handler class
     *
     * @param description description text.
     * @return fluent
     */
    public Protocol<RequestType> setDescription(String description) {
        this.description = description;
        this.emitDocumentation = true;
        return this;
    }

    /**
     * @return a list of all registered routes in the protocol.
     */
    public Set<String> available() {
        return authorizer.list().stream().map(Route::getName).collect(Collectors.toSet());
    }
}

