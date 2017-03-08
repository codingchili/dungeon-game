package com.codingchili.realmregistry.controller;

import com.codingchili.realmregistry.configuration.RealmSettings;

import com.codingchili.core.protocol.*;
import com.codingchili.core.security.Token;

import static com.codingchili.common.Strings.*;

/**
 * @author Robin Duda
 */
class RealmRequest extends ClusterRequest {
    private RealmSettings realm = new RealmSettings();

    RealmRequest(Request request) {
        super(request);

        parseRealm();
    }

    private void parseRealm() {
        if (data().containsKey(ID_REALM)) {
            realm = Serializer.unpack(data().getJsonObject(ID_REALM), RealmSettings.class);
        } else {
            realm = new RealmSettings();

            if (data().containsKey(ID_TOKEN)) {
                realm.setAuthentication(Serializer.unpack(data().getJsonObject(ID_TOKEN), Token.class));
            }
        }
    }

    public Token token() {
        return realm.getAuthentication();
    }

    public RealmSettings getRealm() {
        return realm;
    }

    public String realmName() {
        return token().getDomain();
    }

    public int players() {
        if (data().containsKey(ID_PLAYERS)) {
            return data().getInteger(ID_PLAYERS);
        } else {
            return 0;
        }
    }

    public String sender() {
        return data().getString(PROTOCOL_CONNECTION);
    }

    public String account() {
        return data().getString(ID_ACCOUNT);
    }

    public String name() {
        return data().getString(ID_CHARACTER);
    }
}