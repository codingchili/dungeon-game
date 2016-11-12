package com.codingchili.core.Security;

import java.io.Serializable;
import java.time.Instant;

import com.codingchili.core.Configuration.Strings;

/**
 * @author Robin Duda
 *
 * Used to authenticate requests between services.
 */
public class Token implements Serializable {
    private String key;
    private long expiry;
    private String domain;

    public Token() {
    }

    public Token(TokenFactory factory, RemoteIdentity identity) {
        this(factory, identity.toString());
    }

    public Token(TokenFactory factory, String domain) {
        try {
            this.domain = domain;
            this.expiry = Instant.now().getEpochSecond() + 3600 * 24 * 7;
            this.key = factory.signToken(domain, this.expiry);
        } catch (Throwable e) {
            throw new RuntimeException(Strings.ERROR_TOKEN_FACTORY);
        }
    }

    public String getKey() {
        return key;
    }

    public Token setKey(String key) {
        this.key = key;
        return this;
    }

    public long getExpiry() {
        return expiry;
    }

    public Token setExpiry(long expiry) {
        this.expiry = expiry;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Token setDomain(String domain) {
        this.domain = domain;
        return this;
    }
}