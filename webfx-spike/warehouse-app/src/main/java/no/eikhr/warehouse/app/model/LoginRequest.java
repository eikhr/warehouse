package no.eikhr.warehouse.app.model;

/** WebFX-compatible port of {@code core.main.LoginRequest}. */
public class LoginRequest {
    private String username, password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
