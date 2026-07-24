package no.eikhr.warehouse.app.model;

/** WebFX-compatible port of {@code core.main.AuthSession}. */
public class AuthSession {
    private User user;
    private String token;

    public AuthSession() {}

    public AuthSession(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() { return user; }
    public void setUser(User v) { user = v; }
    public String getToken() { return token; }
    public void setToken(String v) { token = v; }
}
