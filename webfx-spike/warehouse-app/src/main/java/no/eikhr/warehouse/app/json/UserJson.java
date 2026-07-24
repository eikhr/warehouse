package no.eikhr.warehouse.app.json;

import no.eikhr.warehouse.app.model.AuthSession;
import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.model.User;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;

/** {@code User}/{@code LoginRequest}/{@code AuthSession} &lt;-&gt; AST-JSON. */
public final class UserJson {
    private UserJson() {}

    public static String loginRequestToJsonString(LoginRequest req) {
        AstObject o = AST.createObject();
        o.set("username", req.getUsername());
        o.set("password", req.getPassword());
        return Json.formatNode(o);
    }

    public static String userToJsonString(User user) {
        AstObject o = AST.createObject();
        o.set("id", user.getId());
        o.set("username", user.getUsername());
        o.set("password", user.getPassword());
        return Json.formatNode(o);
    }

    public static User userFromAst(ReadOnlyAstObject o) {
        if (o == null) return null;
        return new User(o.getString("id"), o.getString("username"), o.getString("password"));
    }

    public static AuthSession authFromAst(ReadOnlyAstObject o) {
        User user = userFromAst(o.getObject("user"));
        return new AuthSession(user, o.getString("token"));
    }
}
