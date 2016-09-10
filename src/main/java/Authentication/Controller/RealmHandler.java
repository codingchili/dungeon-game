package Authentication.Controller;

import Authentication.Configuration.AuthProvider;
import Configuration.Strings;
import Protocols.AuthorizationHandler.Access;
import Authentication.Model.AsyncAccountStore;
import Authentication.Model.RealmStore;
import Authentication.Configuration.AuthServerSettings;
import Realm.Configuration.RealmSettings;
import Realm.Model.PlayerCharacter;
import Protocols.Realm.CharacterRequest;
import Protocols.Realm.CharacterResponse;
import Protocols.Authentication.RealmRegister;
import Protocols.Authentication.RealmUpdate;
import io.vertx.core.Future;

/**
 * @author Robin Duda
 *         Router used to authenticate realms and generate realmName lists.
 */
public class RealmHandler {
    private RealmStore realmStore;
    private AsyncAccountStore accounts;
    private AuthServerSettings settings;

    public RealmHandler(AuthProvider provider) {
        this.accounts = provider.getAccountStore();
        this.settings = provider.getAuthserverSettings();
        this.realmStore = new RealmStore(provider.getVertx());

        provider.realmProtocol()
                .use(RealmUpdate.ACTION, this::update)
                .use(CharacterRequest.ACTION, this::character)
                .use(Strings.CLIENT_CLOSE, this::disconnected)
                .use(Strings.REALM_AUTHENTICATE, this::register, Access.PUBLIC);
    }

    private void register(RealmRequest request) {
        RealmSettings realm = request.realm();

        realm.setTrusted(settings.isTrustedRealm(realm.getName()));
        realmStore.put(realm);
        request.write(new RealmRegister(true));
    }

    private void update(RealmRequest request) {
        Future<Void> updateFuture = Future.future();
        String realmName = request.realmName();
        int players = request.players();

        updateFuture.setHandler(update -> {
            if (update.succeeded()) {
                request.write(new RealmRegister(true));
            } else {
                request.error();
            }
        });

        realmStore.update(updateFuture, realmName, players);
    }

    private void disconnected(RealmRequest request) {
        realmStore.remove(request.realm().getName());
    }

    private void character(RealmRequest request) {
        Future<PlayerCharacter> find = Future.future();

        find.setHandler(result -> {
            if (result.succeeded()) {
                request.write(new CharacterResponse(result.result(), request.sender()));
            } else
                request.error();
        });
        accounts.findCharacter(find, request.realmName(), request.account(), request.name());
    }
}