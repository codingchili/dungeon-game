package com.codingchili.core.configuration;

/**
 * Simple configurable that maps to a json object.
 */
public class AttributeConfigurable extends Attributes implements Configurable {
    private String path;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public AttributeConfigurable setPath(String path) {
        this.path = path;
        return this;
    }
}
