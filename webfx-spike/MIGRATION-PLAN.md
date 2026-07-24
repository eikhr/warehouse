# Warehouse JavaFX → Web (WebFX) Migration Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the warehouse JavaFX desktop client to a browser web app using WebFX, reusing the domain model, replacing the `java.net.http` + Jackson data layer with WebFX Fetch + AST-JSON, and rebuilding the views programmatically (no FXML).

**Architecture:** Build on the completed spike at `webfx-spike/` (a WebFX multi-module project: shared `warehouse-app` holds JavaFX code; `warehouse-app-gwt` transpiles to JS; `warehouse-app-openjfx` runs on the JVM for tests/desktop). All new code lives in `warehouse-app`. The desktop client's `core.main`, `core.client`, and `ui` modules are NOT reused as-is — their Jackson/`java.net.http`/FXML/barbecue dependencies do not transpile. Instead we port equivalents into `warehouse-app` that use only WebFX-compatible APIs. The spike already proved the two riskiest layers (Fetch networking + AST-JSON parsing) work in-browser against `https://warehouse.eikhr.no`.

**Tech Stack:** JavaFX 17 (WebFX kit subset), WebFX CLI + GWT, `webfx-platform-fetch-ast-json` (Fetch + JSON), `webfx-platform-async` (`Future`), JUnit 5 (JVM-side tests via the openjfx target). Toolchain is provisioned per-command with `nix-shell -p jdk17 maven git` (no permanent install).

## Global Constraints

- **No reflection, no Jackson, no `java.net.http`, no `java.time` in `warehouse-app`.** These do not transpile. Use WebFX AST-JSON, WebFX `Fetch`, and represent timestamps as `String` (ISO-8601, as the server already sends them).
- **Only WebFX-supported controls.** Available: `Button`, `CheckBox`, `RadioButton`, `ToggleGroup`, `ContextMenu`, `Label`, `Hyperlink`, `TextField`, `TextArea`, `PasswordField`, `ProgressBar`, `Slider`, `ScrollPane`, `SplitPane`, `TabPane`, plus `VBox`/`HBox`/`BorderPane`/`GridPane` layout. **No `ComboBox`, `TableView`, `TreeView`, `ListView`.**
- **Async model is WebFX `Future`**, not `java.util.concurrent.CompletableFuture`. `Future<T>` has `.onSuccess(Consumer<T>)`, `.onFailure(Consumer<Throwable>)`, `.map(...)`, `.compose(...)`.
- **Package root:** `no.eikhr.warehouse.app` (the spike's app module).
- **Server contract (unchanged):** base `https://warehouse.eikhr.no`; endpoints under `/warehouse`: `GET /warehouse/items`, `GET /warehouse/item/{id}`, `PUT /warehouse/item/{id}` (header `auth-token`), `DELETE /warehouse/item/{id}` (header `auth-token`), `POST /warehouse/user/login`, `POST /warehouse/user/register`. Server sends `@CrossOrigin`, so browser fetches are allowed.
- **Every build/test command runs inside** `nix-shell -p jdk17 maven git --run "..."`. The WebFX CLI fat jar is at `<scratchpad>/webfx-cli/target/webfx-cli-0.1.0-SNAPSHOT-fat.jar` (rebuild from `github.com/webfx-project/webfx-cli` with `mvn package` if absent). Reference it below as `$WEBFX` = `java -jar <that-jar>`.
- **Commit after every task.** Work on branch `webfx-spike` of `eikhr/warehouse`.

## File Structure

All paths under `webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/`:

- `model/Item.java` — plain data class; `creationDate` is `String`.
- `model/User.java`, `model/AuthSession.java`, `model/LoginRequest.java` — plain data classes.
- `json/ItemJson.java` — `Item` ↔ `ReadOnlyAstObject`/JSON string (replaces Jackson for items).
- `json/UserJson.java` — `User`/`LoginRequest`/`AuthSession` ↔ JSON.
- `client/WarehouseServer.java` — Fetch-based client, one method per endpoint (replaces `RemoteWarehouseServer`).
- `client/ServerException.java` — carries HTTP status + message (replaces `ServerError`).
- `session/Session.java` — holds the current `AuthSession` in memory.
- `ui/AppShell.java` — root `BorderPane` + view switching (replaces FXML scene loading).
- `ui/ServerSelectView.java`, `ui/LoginView.java`, `ui/RegisterView.java`, `ui/ItemListView.java`, `ui/ItemDetailView.java` — programmatic views (replace the 5 FXML files + controllers).
- `WarehouseApp.java` — `Application` entry point; wires `AppShell`.

Tests under `webfx-spike/warehouse-app/src/test/java/no/eikhr/warehouse/app/`.

**Explicitly dropped for the web MVP (documented decisions, not omissions):**
- **Barcode image generation** (`BarcodeCreator`, `barbecue`): the barcode *string* field is kept and editable, but no barcode image is rendered. Barbecue is a native Java imaging library that cannot transpile. Revisit later with a JS barcode library (e.g. JsBarcode) via a WebFX peer if desired.
- **`ComboBox` sort control** in `WarehouseController`: replaced by `RadioButton`s in a `ToggleGroup` (Task 7).

---

## Testing notes (read before Task 1)

Unit tests run **on the JVM**, not transpiled, via the openjfx target which has all JRE providers (including WebFX's JRE Fetch/JSON providers). Add JUnit 5 as a test dependency by declaring it in `warehouse-app/webfx.xml` inside a `<maven-manual-dependencies>` block (WebFX preserves manual Maven deps when regenerating the pom):

```xml
<maven-manual-dependencies>
    <group-artifact-version scope="test">org.junit.jupiter:junit-jupiter:5.10.2</group-artifact-version>
</maven-manual-dependencies>
```

Run model/serialization/client tests with:
`nix-shell -p jdk17 maven git --run "cd webfx-spike && $WEBFX update && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true"`

If WebFX's generated pom resists test dependencies, the fallback is a standalone JUnit run: compile `warehouse-app/src/{main,test}` against the resolved classpath and run `junit-platform-console-standalone`. Prefer the `mvn test` path; only fall back if it fails.

UI tasks (4–8) have no unit tests (WebFX UI testing would require TestFX, which does not transpile). Their verification is: **transpile succeeds + the app renders correctly in a browser**, checked with the serve-and-Playwright loop from the spike:
```
python3 -m http.server 8099 --directory warehouse-app-gwt/target/warehouse-app-gwt-1.0.0-SNAPSHOT/warehouse_app_gwt   # background
# navigate a browser to http://localhost:8099/ and confirm the rendered UI
```

---

## Task 1: WebFX-compatible domain model

**Files:**
- Create: `warehouse-app/src/main/java/no/eikhr/warehouse/app/model/Item.java`
- Create: `.../model/User.java`, `.../model/AuthSession.java`, `.../model/LoginRequest.java`
- Test: `warehouse-app/src/test/java/no/eikhr/warehouse/app/model/ItemTest.java`

**Interfaces:**
- Produces: `Item` with fields `id, name, amount, barcode, brand, regularPrice, salePrice, purchasePrice, section, row, shelf, height, width, length, weight` (String/int/Double) and `creationDate` (**String**, ISO-8601). Getters/setters for all; no-arg constructor. `User(id, username, password)` getters. `AuthSession(User user, String token)` with `getUser()`, `getToken()`. `LoginRequest(username, password)` getters.

- [ ] **Step 1: Write the failing test**

```java
package no.eikhr.warehouse.app.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    @Test
    void gettersAndSettersRoundTrip() {
        Item i = new Item();
        i.setId("abc");
        i.setName("Jägermeister");
        i.setAmount(3);
        i.setBrand("Mast-Jägermeister");
        i.setRegularPrice(1.5);
        i.setCreationDate("2022-11-26T20:43:23.206");
        assertEquals("abc", i.getId());
        assertEquals("Jägermeister", i.getName());
        assertEquals(3, i.getAmount());
        assertEquals("Mast-Jägermeister", i.getBrand());
        assertEquals(1.5, i.getRegularPrice());
        assertEquals("2022-11-26T20:43:23.206", i.getCreationDate());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=ItemTest"`
Expected: FAIL — `Item` does not exist / no such methods.

- [ ] **Step 3: Write minimal implementation**

```java
package no.eikhr.warehouse.app.model;

public class Item {
    private String id;
    private String name;
    private int amount;
    private String barcode;
    private String brand;
    private Double regularPrice;
    private Double salePrice;
    private Double purchasePrice;
    private String section;
    private String row;
    private String shelf;
    private Double height;
    private Double width;
    private Double length;
    private Double weight;
    private String creationDate; // ISO-8601 string; java.time does not transpile

    public Item() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public int getAmount() { return amount; }
    public void setAmount(int v) { amount = v; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String v) { barcode = v; }
    public String getBrand() { return brand; }
    public void setBrand(String v) { brand = v; }
    public Double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(Double v) { regularPrice = v; }
    public Double getSalePrice() { return salePrice; }
    public void setSalePrice(Double v) { salePrice = v; }
    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double v) { purchasePrice = v; }
    public String getSection() { return section; }
    public void setSection(String v) { section = v; }
    public String getRow() { return row; }
    public void setRow(String v) { row = v; }
    public String getShelf() { return shelf; }
    public void setShelf(String v) { shelf = v; }
    public Double getHeight() { return height; }
    public void setHeight(Double v) { height = v; }
    public Double getWidth() { return width; }
    public void setWidth(Double v) { width = v; }
    public Double getLength() { return length; }
    public void setLength(Double v) { length = v; }
    public Double getWeight() { return weight; }
    public void setWeight(Double v) { weight = v; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String v) { creationDate = v; }
}
```

Also create `User`, `AuthSession`, `LoginRequest` (no Jackson annotations):

```java
package no.eikhr.warehouse.app.model;
public class User {
    private String id, username, password;
    public User() {}
    public User(String id, String username, String password) { this.id = id; this.username = username; this.password = password; }
    public String getId() { return id; }
    public void setId(String v) { id = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
}
```
```java
package no.eikhr.warehouse.app.model;
public class AuthSession {
    private User user;
    private String token;
    public AuthSession() {}
    public AuthSession(User user, String token) { this.user = user; this.token = token; }
    public User getUser() { return user; }
    public void setUser(User v) { user = v; }
    public String getToken() { return token; }
    public void setToken(String v) { token = v; }
}
```
```java
package no.eikhr.warehouse.app.model;
public class LoginRequest {
    private String username, password;
    public LoginRequest() {}
    public LoginRequest(String username, String password) { this.username = username; this.password = password; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=ItemTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/model \
        webfx-spike/warehouse-app/src/test/java/no/eikhr/warehouse/app/model
git commit -m "feat(webfx): WebFX-compatible domain model (no Jackson, String dates)"
```

---

## Task 2: JSON serialization (replaces Jackson)

**Files:**
- Create: `warehouse-app/src/main/java/no/eikhr/warehouse/app/json/ItemJson.java`
- Create: `.../json/UserJson.java`
- Test: `warehouse-app/src/test/java/no/eikhr/warehouse/app/json/ItemJsonTest.java`

**Interfaces:**
- Consumes: `Item`, `User`, `LoginRequest`, `AuthSession` (Task 1); WebFX `dev.webfx.platform.ast.*` and `dev.webfx.platform.ast.json.Json`.
- Produces: `ItemJson.fromAst(ReadOnlyAstObject) -> Item`, `ItemJson.listFromAst(ReadOnlyAstArray) -> List<Item>`, `ItemJson.toJsonString(Item) -> String`. `UserJson.loginRequestToJsonString(LoginRequest) -> String`, `UserJson.userToJsonString(User) -> String`, `UserJson.authFromAst(ReadOnlyAstObject) -> AuthSession`.

- [ ] **Step 1: Write the failing test**

```java
package no.eikhr.warehouse.app.json;

import no.eikhr.warehouse.app.model.Item;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemJsonTest {
    @Test
    void parsesServerItemJson() {
        String body = "{\"id\":\"abc\",\"name\":\"Vodka 40% vann\",\"amount\":3,"
                    + "\"brand\":\"Koskenkorva\",\"regularPrice\":1.5,\"barcode\":null,"
                    + "\"creationDate\":\"2022-11-26T20:43:23.206\"}";
        ReadOnlyAstObject obj = Json.parseObject(body);
        Item i = ItemJson.fromAst(obj);
        assertEquals("abc", i.getId());
        assertEquals("Vodka 40% vann", i.getName());
        assertEquals(3, i.getAmount());
        assertEquals("Koskenkorva", i.getBrand());
        assertEquals(1.5, i.getRegularPrice());
        assertNull(i.getBarcode());
    }

    @Test
    void serializesItemToJsonRoundTrip() {
        Item i = new Item();
        i.setId("x1"); i.setName("Test"); i.setAmount(7); i.setRegularPrice(2.0);
        String json = ItemJson.toJsonString(i);
        Item back = ItemJson.fromAst(Json.parseObject(json));
        assertEquals("x1", back.getId());
        assertEquals("Test", back.getName());
        assertEquals(7, back.getAmount());
        assertEquals(2.0, back.getRegularPrice());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=ItemJsonTest"`
Expected: FAIL — `ItemJson` not found.

- [ ] **Step 3: Write minimal implementation**

```java
package no.eikhr.warehouse.app.json;

import no.eikhr.warehouse.app.model.Item;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.ast.json.Json;
import java.util.ArrayList;
import java.util.List;

public final class ItemJson {
    private ItemJson() {}

    public static Item fromAst(ReadOnlyAstObject o) {
        Item i = new Item();
        i.setId(o.getString("id"));
        i.setName(o.getString("name"));
        i.setAmount(o.getInteger("amount", 0));
        i.setBarcode(o.getString("barcode"));
        i.setBrand(o.getString("brand"));
        i.setRegularPrice(o.getDouble("regularPrice"));
        i.setSalePrice(o.getDouble("salePrice"));
        i.setPurchasePrice(o.getDouble("purchasePrice"));
        i.setSection(o.getString("section"));
        i.setRow(o.getString("row"));
        i.setShelf(o.getString("shelf"));
        i.setHeight(o.getDouble("height"));
        i.setWidth(o.getDouble("width"));
        i.setLength(o.getDouble("length"));
        i.setWeight(o.getDouble("weight"));
        i.setCreationDate(o.getString("creationDate"));
        return i;
    }

    public static List<Item> listFromAst(ReadOnlyAstArray arr) {
        List<Item> list = new ArrayList<>();
        for (int k = 0; k < arr.size(); k++)
            list.add(fromAst(arr.getObject(k)));
        return list;
    }

    public static String toJsonString(Item i) {
        AstObject o = AST.createObject();
        o.set("id", i.getId());
        o.set("name", i.getName());
        o.set("amount", i.getAmount());
        o.set("barcode", i.getBarcode());
        o.set("brand", i.getBrand());
        o.set("regularPrice", i.getRegularPrice());
        o.set("salePrice", i.getSalePrice());
        o.set("purchasePrice", i.getPurchasePrice());
        o.set("section", i.getSection());
        o.set("row", i.getRow());
        o.set("shelf", i.getShelf());
        o.set("height", i.getHeight());
        o.set("width", i.getWidth());
        o.set("length", i.getLength());
        o.set("weight", i.getWeight());
        o.set("creationDate", i.getCreationDate());
        return Json.formatNode(o);
    }
}
```

> NOTE for implementer: verify the exact AST builder API by reading
> `webfx-platform/webfx-platform-ast/src/main/java/dev/webfx/platform/ast/AST.java`
> and `AstObject.java` (methods `createObject()`, `set(String,Object)`) and the
> `Json.formatNode`/`Json.formatObject` name in
> `webfx-platform-ast-json/.../json/Json.java`. If a name differs, use the actual
> one — do not invent. Then create `UserJson` with the same pattern:
> `loginRequestToJsonString`, `userToJsonString`, and `authFromAst` (read nested
> `user` object + `token` string).

- [ ] **Step 4: Run test to verify it passes**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=ItemJsonTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/json \
        webfx-spike/warehouse-app/src/test/java/no/eikhr/warehouse/app/json
git commit -m "feat(webfx): AST-JSON item/user serialization (replaces Jackson)"
```

---

## Task 3: Fetch-based server client (replaces RemoteWarehouseServer)

**Files:**
- Create: `warehouse-app/src/main/java/no/eikhr/warehouse/app/client/WarehouseServer.java`
- Create: `.../client/ServerException.java`
- Test (integration, hits live server): `warehouse-app/src/test/java/no/eikhr/warehouse/app/client/WarehouseServerIT.java`

**Interfaces:**
- Consumes: `Item`, `AuthSession`, `LoginRequest`, `User`, `ItemJson`, `UserJson`; `dev.webfx.platform.fetch.Fetch`, `dev.webfx.platform.fetch.FetchOptions`, `dev.webfx.platform.fetch.json.JsonFetch`, `dev.webfx.platform.async.Future`.
- Produces: `new WarehouseServer(String baseUrl)`; `Future<List<Item>> getItems()`, `Future<Item> getItem(String id)`, `Future<Void> putItem(Item item, AuthSession auth)`, `Future<Void> removeItem(String id, AuthSession auth)`, `Future<AuthSession> login(LoginRequest req)`, `Future<Void> register(User user)`. All URLs are `baseUrl + "/warehouse/..."`.

- [ ] **Step 1: Write the failing test**

```java
package no.eikhr.warehouse.app.client;

import no.eikhr.warehouse.app.model.Item;
import dev.webfx.platform.async.Future;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class WarehouseServerIT {
    @Test
    void getItemsReturnsLiveInventory() throws Exception {
        WarehouseServer server = new WarehouseServer("https://warehouse.eikhr.no");
        AtomicReference<List<Item>> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        server.getItems()
            .onFailure(e -> { error.set(e); done.countDown(); })
            .onSuccess(items -> { result.set(items); done.countDown(); });
        assertTrue(done.await(15, TimeUnit.SECONDS), "request timed out");
        assertNull(error.get(), () -> "request failed: " + error.get());
        assertNotNull(result.get());
        assertFalse(result.get().isEmpty(), "expected at least one item");
        assertNotNull(result.get().get(0).getName());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=WarehouseServerIT"`
Expected: FAIL — `WarehouseServer` not found.

- [ ] **Step 3: Write minimal implementation**

```java
package no.eikhr.warehouse.app.client;

import no.eikhr.warehouse.app.json.ItemJson;
import no.eikhr.warehouse.app.json.UserJson;
import no.eikhr.warehouse.app.model.*;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.fetch.FetchOptions;
import dev.webfx.platform.fetch.Response;
import dev.webfx.platform.fetch.json.JsonFetch;
import java.util.List;

public class WarehouseServer {
    private final String baseUrl;
    public WarehouseServer(String baseUrl) { this.baseUrl = baseUrl; }

    private String url(String... segs) {
        StringBuilder b = new StringBuilder(baseUrl).append("/warehouse");
        for (String s : segs) b.append('/').append(s);
        return b.toString();
    }

    public Future<List<Item>> getItems() {
        return JsonFetch.fetchJsonArray(url("items")).map(ItemJson::listFromAst);
    }

    public Future<Item> getItem(String id) {
        return JsonFetch.fetchJsonObject(url("item", id)).map(ItemJson::fromAst);
    }

    public Future<Void> putItem(Item item, AuthSession auth) {
        FetchOptions opts = FetchOptions.create()
            .setMethod("PUT")
            .setHeaders("Content-Type", "application/json", "auth-token", auth.getToken())
            .setBody(ItemJson.toJsonString(item));
        return Fetch.fetch(url("item", item.getId()), opts).compose(WarehouseServer::checkOk);
    }

    public Future<Void> removeItem(String id, AuthSession auth) {
        FetchOptions opts = FetchOptions.create()
            .setMethod("DELETE")
            .setHeaders("auth-token", auth.getToken());
        return Fetch.fetch(url("item", id), opts).compose(WarehouseServer::checkOk);
    }

    public Future<AuthSession> login(LoginRequest req) {
        FetchOptions opts = FetchOptions.create()
            .setMethod("POST")
            .setHeaders("Content-Type", "application/json")
            .setBody(UserJson.loginRequestToJsonString(req));
        return JsonFetch.fetchJsonObject(url("user", "login"), opts).map(UserJson::authFromAst);
    }

    public Future<Void> register(User user) {
        FetchOptions opts = FetchOptions.create()
            .setMethod("POST")
            .setHeaders("Content-Type", "application/json")
            .setBody(UserJson.userToJsonString(user));
        return Fetch.fetch(url("user", "register"), opts).compose(WarehouseServer::checkOk);
    }

    private static Future<Void> checkOk(Response r) {
        if (r.ok()) return Future.succeededFuture(null);
        return Future.failedFuture(new ServerException(r.status(), "HTTP " + r.status()));
    }
}
```

> NOTE for implementer: `FetchOptions` builder method names (`create`,
> `setMethod`, `setHeaders`, `setBody`) and `Response.ok()`/`Response.status()`
> must be verified against
> `webfx-platform/webfx-platform-fetch/src/main/java/dev/webfx/platform/fetch/{FetchOptions,Response}.java`.
> Use the real names. `JsonFetch.fetchJsonObject(url, opts)` and
> `fetchJsonArray(url, opts)` signatures were confirmed during the spike.
> Also create `ServerException extends RuntimeException` with `int status`.

- [ ] **Step 4: Run test to verify it passes**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && mvn -q test -pl warehouse-app -am -Dwebfx.skip=true -Dtest=WarehouseServerIT"`
Expected: PASS — fetches live items from `warehouse.eikhr.no` (JRE Fetch provider).

- [ ] **Step 5: Commit**

```bash
git add webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/client \
        webfx-spike/warehouse-app/src/test/java/no/eikhr/warehouse/app/client
git commit -m "feat(webfx): Fetch-based WarehouseServer client (replaces java.net.http)"
```

---

## Task 4: App shell + view navigation

Replaces FXML scene-loading. `AppShell` owns a `BorderPane` and swaps the center node. Views are plain classes exposing a `Node getRoot()`.

**Files:**
- Create: `.../session/Session.java` (holds `AuthSession current` + `String baseUrl` + `WarehouseServer server`)
- Create: `.../ui/AppShell.java`
- Modify: `warehouse-app/src/main/java/no/eikhr/warehouse/app/WarehouseApp.java` (replace spike body)

**Interfaces:**
- Produces: `Session` singleton-ish holder passed to views; `AppShell.show(View view)` sets center; `interface View { javafx.scene.Node getRoot(); }`.

- [ ] **Step 1: Write `Session`, `View`, `AppShell`, and wire `WarehouseApp`**

```java
// session/Session.java
package no.eikhr.warehouse.app.session;
import no.eikhr.warehouse.app.client.WarehouseServer;
import no.eikhr.warehouse.app.model.AuthSession;
public class Session {
    private String baseUrl = "https://warehouse.eikhr.no";
    private WarehouseServer server = new WarehouseServer(baseUrl);
    private AuthSession auth;
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String u) { baseUrl = u; server = new WarehouseServer(u); }
    public WarehouseServer server() { return server; }
    public AuthSession getAuth() { return auth; }
    public void setAuth(AuthSession a) { auth = a; }
    public boolean isLoggedIn() { return auth != null; }
}
```
```java
// ui/View.java
package no.eikhr.warehouse.app.ui;
import javafx.scene.Node;
public interface View { Node getRoot(); }
```
```java
// ui/AppShell.java
package no.eikhr.warehouse.app.ui;
import javafx.scene.layout.BorderPane;
public class AppShell {
    private final BorderPane root = new BorderPane();
    public BorderPane getRoot() { return root; }
    public void show(View view) { root.setCenter(view.getRoot()); }
}
```
```java
// WarehouseApp.java  (replace the spike's body)
package no.eikhr.warehouse.app;
import no.eikhr.warehouse.app.session.Session;
import no.eikhr.warehouse.app.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class WarehouseApp extends Application {
    @Override public void start(Stage stage) {
        Session session = new Session();
        AppShell shell = new AppShell();
        shell.show(new ServerSelectView(session, shell)); // Task 5
        stage.setScene(new Scene(shell.getRoot(), 900, 640));
        stage.setTitle("Warehouse");
        stage.show();
    }
}
```

- [ ] **Step 2: Verify it transpiles (no unit test — UI wiring)**

Run: `nix-shell -p jdk17 maven git --run "cd webfx-spike && $WEBFX build --gwt 2>&1 | tail -5"`
Expected: `BUILD SUCCESS` (will fail to *link* until Task 5 exists; if so, temporarily stub `ServerSelectView` returning an empty `VBox` and proceed — remove the stub in Task 5).

- [ ] **Step 3: Commit**

```bash
git add webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/session \
        webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/ui/View.java \
        webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/ui/AppShell.java \
        webfx-spike/warehouse-app/src/main/java/no/eikhr/warehouse/app/WarehouseApp.java
git commit -m "feat(webfx): app shell + session + view navigation"
```

---

## Task 5: ServerSelect view

Port of `ServerSelect.fxml`/`ServerSelectController`. TextField for URL + "Connect" button → sets `session.setBaseUrl(...)` and navigates to `LoginView`.

**Files:** Create `.../ui/ServerSelectView.java`. **Interfaces:** `new ServerSelectView(Session, AppShell)` implements `View`.

- [ ] **Step 1: Implement**

```java
package no.eikhr.warehouse.app.ui;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ServerSelectView implements View {
    private final VBox root = new VBox(10);
    public ServerSelectView(Session session, AppShell shell) {
        TextField url = new TextField(session.getBaseUrl());
        Button connect = new Button("Connect");
        Label error = new Label();
        connect.setOnAction(e -> {
            session.setBaseUrl(url.getText().trim());
            shell.show(new LoginView(session, shell)); // Task 6
        });
        root.setPadding(new Insets(20));
        root.getChildren().addAll(new Label("Server URL:"), url, connect, error);
    }
    @Override public Node getRoot() { return root; }
}
```

- [ ] **Step 2: Transpile + browser check.** Build (`$WEBFX build --gwt`), serve, load `http://localhost:8099/`, confirm the URL field + Connect button render.
- [ ] **Step 3: Commit** `git commit -m "feat(webfx): ServerSelect view"`

---

## Task 6: Login + Register views

Port of `Login.fxml`/`LoginController` and `Register.fxml`/`RegisterController`. Uses `PasswordField` (WebFX-supported). On success, `session.setAuth(...)` and go to `ItemListView`.

**Files:** Create `.../ui/LoginView.java`, `.../ui/RegisterView.java`. **Interfaces:** `new LoginView(Session, AppShell)`, `new RegisterView(Session, AppShell)` implement `View`.

- [ ] **Step 1: Implement `LoginView`**

```java
package no.eikhr.warehouse.app.ui;
import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginView implements View {
    private final VBox root = new VBox(10);
    public LoginView(Session session, AppShell shell) {
        TextField user = new TextField();
        user.setPromptText("username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("password");
        Button login = new Button("Log in");
        Hyperlink toRegister = new Hyperlink("Create account");
        Label error = new Label();
        login.setOnAction(e -> {
            error.setText("Logging in…");
            session.server().login(new LoginRequest(user.getText(), pass.getText()))
                .onFailure(err -> error.setText("Login failed: " + err.getMessage()))
                .onSuccess(auth -> { session.setAuth(auth); shell.show(new ItemListView(session, shell)); });
        });
        toRegister.setOnAction(e -> shell.show(new RegisterView(session, shell)));
        root.setPadding(new Insets(20));
        root.getChildren().addAll(new Label("Log in"), user, pass, login, toRegister, error);
    }
    @Override public Node getRoot() { return root; }
}
```

- [ ] **Step 2: Implement `RegisterView`** — same shape: username + two `PasswordField`s (password + confirm), a "Register" button calling `session.server().register(new User(null, username, password))`, on success navigate to `LoginView`; a `Hyperlink` back to login. (Write the full class following the `LoginView` pattern above, substituting the register call and the confirm-password equality check that sets `error` when the two fields differ.)
- [ ] **Step 3: Transpile + browser check** — confirm login form renders, password field masks input.
- [ ] **Step 4: Commit** `git commit -m "feat(webfx): login + register views"`

---

## Task 7: Item list view (sort via RadioButtons, not ComboBox)

Port of `Warehouse.fxml`/`WarehouseController`. Fetches items, renders each as a clickable row (`Hyperlink` or `Button`) inside a `ScrollPane`→`VBox`. The `ComboBox` sort selector is replaced by a `ToggleGroup` of `RadioButton`s (Name / Amount / Brand). Clicking a row opens `ItemDetailView`.

**Files:** Create `.../ui/ItemListView.java`. **Interfaces:** `new ItemListView(Session, AppShell)` implements `View`. Consumes `session.server().getItems()`.

- [ ] **Step 1: Implement**

```java
package no.eikhr.warehouse.app.ui;
import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.Comparator;
import java.util.List;

public class ItemListView implements View {
    private final BorderPane root = new BorderPane();
    private final VBox rows = new VBox(4);
    private final Label status = new Label();
    private final Session session;
    private final AppShell shell;
    private List<Item> items = java.util.Collections.emptyList();

    public ItemListView(Session session, AppShell shell) {
        this.session = session; this.shell = shell;

        ToggleGroup sort = new ToggleGroup();
        RadioButton byName = new RadioButton("Name");  byName.setToggleGroup(sort); byName.setSelected(true);
        RadioButton byAmount = new RadioButton("Amount"); byAmount.setToggleGroup(sort);
        RadioButton byBrand = new RadioButton("Brand"); byBrand.setToggleGroup(sort);
        byName.setOnAction(e -> render(Comparator.comparing(i -> nz(i.getName()))));
        byAmount.setOnAction(e -> render(Comparator.comparingInt(Item::getAmount)));
        byBrand.setOnAction(e -> render(Comparator.comparing(i -> nz(i.getBrand()))));

        Button add = new Button("Add item");
        add.setOnAction(e -> shell.show(new ItemDetailView(session, shell, new Item())));

        HBox top = new HBox(10, new Label("Sort:"), byName, byAmount, byBrand, add);
        top.setPadding(new Insets(10));
        rows.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(rows); scroll.setFitToWidth(true);
        root.setTop(new VBox(top, status));
        root.setCenter(scroll);

        load();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private void load() {
        status.setText("Loading…");
        session.server().getItems()
            .onFailure(err -> status.setText("Error: " + err.getMessage()))
            .onSuccess(list -> { items = list; status.setText(list.size() + " items");
                                 render(Comparator.comparing(i -> nz(i.getName()))); });
    }

    private void render(Comparator<Item> cmp) {
        items.sort(cmp);
        rows.getChildren().clear();
        for (Item it : items) {
            Hyperlink row = new Hyperlink(nz(it.getName()) + "  —  qty " + it.getAmount());
            row.setOnAction(e -> shell.show(new ItemDetailView(session, shell, it)));
            rows.getChildren().add(row);
        }
    }
    @Override public Node getRoot() { return root; }
}
```

- [ ] **Step 2: Transpile + browser check** — confirm the item list renders from live data and the sort radio buttons reorder it.
- [ ] **Step 3: Commit** `git commit -m "feat(webfx): item list view with radio-button sort (replaces ComboBox)"`

---

## Task 8: Item detail view (add/edit; barcode image dropped)

Port of `DetailsView.fxml`/`DetailsViewController`. A `GridPane` form of `TextField`s for the item fields, a Save button (`putItem`, requires auth), a Delete button (`removeItem`), and Back. **No barcode image** — the `barcode` field is a plain `TextField`; barbecue is not ported (documented in the plan header).

**Files:** Create `.../ui/ItemDetailView.java`. **Interfaces:** `new ItemDetailView(Session, AppShell, Item)` implements `View`.

- [ ] **Step 1: Implement** — a `GridPane` binding `TextField`s to `name, amount, barcode, brand, regularPrice, salePrice, purchasePrice, section, row, shelf`. Save reads fields back into the `Item` (parse numbers with `try/catch` → show error label on bad input), then:
```java
save.setOnAction(e -> {
    if (!session.isLoggedIn()) { error.setText("Log in to save"); return; }
    // ...copy TextField values into item, parsing Double/int...
    session.server().putItem(item, session.getAuth())
        .onFailure(err -> error.setText("Save failed: " + err.getMessage()))
        .onSuccess(v -> shell.show(new ItemListView(session, shell)));
});
```
Delete calls `session.server().removeItem(item.getId(), session.getAuth())` then navigates to `ItemListView`. Back navigates to `ItemListView`. (Write the full class following the `ItemListView`/`LoginView` patterns — `GridPane.add(node, col, row)` for layout, one `TextField` per field, the Save/Delete/Back buttons wired as above.)

- [ ] **Step 2: Transpile + browser check** — open an item from the list, confirm fields populate; (auth-gated Save/Delete verified in Task 9).
- [ ] **Step 3: Commit** `git commit -m "feat(webfx): item detail view (add/edit; barcode image dropped)"`

---

## Task 9: End-to-end auth flow verification

No new production code beyond wiring already present. Verify the protected round-trip works in the browser against the live server: ServerSelect → Register a throwaway user → Login → item list loads → open item → edit qty → Save → list reflects change.

- [ ] **Step 1:** Build (`$WEBFX build --gwt`), serve on `:8099`, drive with a browser (Playwright): register `spiketest`/`spiketest`, log in, confirm 22+ items load, open one, change amount, Save, confirm the list shows the new amount. Capture a screenshot to `webfx-e2e.png`.
- [ ] **Step 2: Commit** any wiring fixes: `git commit -m "fix(webfx): auth round-trip wiring"`

---

## Task 10: Full build, all targets, and deploy option

- [ ] **Step 1:** `nix-shell -p jdk17 maven git --run "cd webfx-spike && $WEBFX build --gwt 2>&1 | tail -8"` → all modules `SUCCESS`.
- [ ] **Step 2 (optional deploy):** the GWT output at `warehouse-app-gwt/target/warehouse-app-gwt-1.0.0-SNAPSHOT/warehouse_app_gwt/` is static — it can be served behind Traefik as a new service `warehouse-webfx.eikhr.no` on the `server-infra` box, mirroring the React `warehouse-webapp` deployment. (Out of scope for this plan; note it as the productionization follow-up.)
- [ ] **Step 3: Commit** `git commit -m "chore(webfx): full multi-target build green"` and open a PR from `webfx-spike` if desired.

---

## Self-review notes

- **Spec coverage:** networking (Task 3), JSON (Task 2), model (Task 1), all 5 FXML views (Tasks 5–8), ComboBox (Task 7), barcode decision (header + Task 8), auth (Tasks 3/6/9). FXML is intentionally *not* converted — views are rebuilt programmatically (the spike-proven path).
- **Known verification gaps for the implementer to close by reading WebFX source (not guess):** exact `AST`/`AstObject`/`Json.formatNode` names (Task 2 note) and exact `FetchOptions`/`Response` builder names (Task 3 note). These are the only unverified API names; the spike confirmed `JsonFetch`, `ReadOnlyAstObject` getters, and `Future.onSuccess/onFailure/map`.
- **Out of scope:** barcode image rendering; offline/local-server mode (`LocalServer`); production deployment (Task 10 step 2 is a pointer only).
