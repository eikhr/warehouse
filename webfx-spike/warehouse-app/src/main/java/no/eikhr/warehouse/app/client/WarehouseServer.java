package no.eikhr.warehouse.app.client;

import no.eikhr.warehouse.app.json.ItemJson;
import no.eikhr.warehouse.app.json.UserJson;
import no.eikhr.warehouse.app.model.AuthSession;
import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.model.User;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.fetch.FetchOptions;
import dev.webfx.platform.fetch.Headers;
import dev.webfx.platform.fetch.Response;
import dev.webfx.platform.fetch.json.JsonFetch;

import java.util.List;

/**
 * Fetch-based warehouse client, one method per endpoint. Replaces
 * {@code ui.RemoteWarehouseServer} (which used {@code java.net.http} + Jackson).
 */
public class WarehouseServer {

    private final String baseUrl;

    public WarehouseServer(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String url(String... segs) {
        StringBuilder b = new StringBuilder(baseUrl).append("/warehouse");
        for (String s : segs) b.append('/').append(s);
        return b.toString();
    }

    public Future<List<Item>> getItems() {
        return JsonFetch.fetchJsonArray(url("items")).map(ItemJson::listFromAst);
    }

    public Future<Item> getItem(String id) {
        return fetchObjectChecked(url("item", id), null).map(ItemJson::fromAst);
    }

    public Future<Void> putItem(Item item, AuthSession auth) {
        FetchOptions opts = new FetchOptions()
            .setMethod("PUT")
            .setHeaders(Headers.create()
                .set("Content-Type", "application/json")
                .set("auth-token", auth.getToken()))
            .setBody(ItemJson.toJsonString(item));
        return Fetch.fetch(url("item", item.getId()), opts).compose(WarehouseServer::checkOk);
    }

    public Future<Void> removeItem(String id, AuthSession auth) {
        FetchOptions opts = new FetchOptions()
            .setMethod("DELETE")
            .setHeaders(Headers.create().set("auth-token", auth.getToken()));
        return Fetch.fetch(url("item", id), opts).compose(WarehouseServer::checkOk);
    }

    public Future<AuthSession> login(LoginRequest req) {
        FetchOptions opts = new FetchOptions()
            .setMethod("POST")
            .setHeaders(Headers.create().set("Content-Type", "application/json"))
            .setBody(UserJson.loginRequestToJsonString(req));
        return fetchObjectChecked(url("user", "login"), opts).map(UserJson::authFromAst);
    }

    public Future<Void> register(User user) {
        FetchOptions opts = new FetchOptions()
            .setMethod("POST")
            .setHeaders(Headers.create().set("Content-Type", "application/json"))
            .setBody(UserJson.userToJsonString(user));
        return Fetch.fetch(url("user", "register"), opts).compose(WarehouseServer::checkOk);
    }

    /** Fetches a JSON object but fails the future (with a {@link ServerException})
     *  on a non-2xx status, unlike {@link JsonFetch} which parses any body. */
    private static Future<ReadOnlyAstObject> fetchObjectChecked(String url, FetchOptions opts) {
        return Fetch.fetch(url, opts).compose(r ->
            r.text().compose(body -> {
                if (r.ok()) {
                    try {
                        return Future.succeededFuture(Json.parseObject(body));
                    } catch (Exception e) {
                        return Future.failedFuture(e);
                    }
                }
                return Future.failedFuture(new ServerException(r.status(), extractMessage(body, r.status())));
            }));
    }

    private static Future<Void> checkOk(Response r) {
        if (r.ok())
            return Future.succeededFuture(null);
        return r.text().compose(body ->
            Future.failedFuture(new ServerException(r.status(), extractMessage(body, r.status()))));
    }

    private static String extractMessage(String body, int status) {
        try {
            ReadOnlyAstObject o = Json.parseObject(body);
            String msg = o.getString("message");
            if (msg == null || msg.isEmpty())
                msg = o.getString("error");
            if (msg != null && !msg.isEmpty())
                return msg;
        } catch (Exception ignored) {
            // body was not a JSON object
        }
        return "HTTP " + status;
    }
}
