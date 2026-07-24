package no.eikhr.warehouse.app.session;

import no.eikhr.warehouse.app.client.WarehouseServer;
import no.eikhr.warehouse.app.model.AuthSession;

/** In-memory holder of the active server + auth session. */
public class Session {
    private String baseUrl = "https://warehouse.eikhr.no";
    private WarehouseServer server = new WarehouseServer(baseUrl);
    private AuthSession auth;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String u) { baseUrl = u; server = new WarehouseServer(u); }
    public WarehouseServer server() { return server; }
    public AuthSession getAuth() { return auth; }
    public void setAuth(AuthSession a) { auth = a; }
    public boolean isLoggedIn() { return auth != null && auth.getToken() != null; }
    public String username() {
        return isLoggedIn() && auth.getUser() != null ? auth.getUser().getUsername() : null;
    }
}
