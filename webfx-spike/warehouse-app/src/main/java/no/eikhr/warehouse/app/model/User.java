package no.eikhr.warehouse.app.model;

/** WebFX-compatible port of {@code core.main.User} (no Jackson, no hashing). */
public class User {
    private String id, username, password;

    public User() {}

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
}
