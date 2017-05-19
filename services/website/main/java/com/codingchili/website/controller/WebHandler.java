package com.codingchili.website.controller;

import com.codingchili.common.Strings;
import com.codingchili.core.configuration.CachedFileStoreSettings;
import com.codingchili.core.files.CachedFileStore;
import com.codingchili.core.files.exception.FileMissingException;
import com.codingchili.core.listener.CoreHandler;
import com.codingchili.core.listener.Request;
import com.codingchili.core.protocol.Access;
import com.codingchili.core.protocol.Protocol;
import com.codingchili.core.protocol.RequestHandler;
import com.codingchili.website.configuration.WebserverContext;

import io.vertx.core.Future;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 *         <p>
 *         Handles incoming requests for files. (website files)
 */
public class WebHandler implements CoreHandler {
    private final Protocol<RequestHandler<Request>> protocol = new Protocol<>();
    private final CachedFileStore files;
    private WebserverContext context;

    public WebHandler(WebserverContext context) {
        this.context = context;

        this.files = new CachedFileStore(context, new CachedFileStoreSettings()
                .setDirectory(context.resources())
                .setGzip(context.isGzip()));

        protocol.use(Strings.ID_PING, Request::accept, Access.PUBLIC)
                .use(ANY, this::serve, Access.PUBLIC);
    }

    private void serve(Request request) {
        try {
            String file = request.route();

            if (file.equals(EMPTY)) {
                file = context.getStartPage();
                context.onPageLoaded(request);
            }

            request.write(files.getFile(file).getBuffer());
        } catch (FileMissingException e) {
            request.error(e);
        }
    }

    @Override
    public void handle(Request request) {
        protocol.get(Access.PUBLIC, request.route()).handle(request);
    }

    @Override
    public void start(Future<Void> start) {
        context.logger().onHandlerStarted(start, this);
    }

    @Override
    public String address() {
        return NODE_WEBSERVER;
    }
}
