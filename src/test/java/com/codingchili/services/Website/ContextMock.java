package com.codingchili.services.Website;

import io.vertx.core.Vertx;

import com.codingchili.core.Configuration.Strings;

import com.codingchili.services.Website.Configuration.WebserverContext;
import com.codingchili.services.Website.Configuration.WebserverSettings;

/**
 * @author Robin Duda
 */
class ContextMock extends WebserverContext {
    public ContextMock(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected WebserverSettings service() {
        return new WebserverSettings();
    }

    @Override
    public boolean isGzip() {
        return service().getGzip();
    }

    @Override
    public String getMissingPage() {
        return "/404.json";
    }

    @Override
    public String getStartPage() {
        return "/index.html";
    }

    @Override
    public String resources() {
        return Strings.testDirectory("Services/website");
    }
}
