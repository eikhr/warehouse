# Warehouse WebFX — Faithful-Match Spec

Rework the existing WebFX views (`warehouse-app/.../ui/`) to closely match the **original JavaFX desktop client**. The parent agent has the original's screenshots and will verify each view visually and send corrections; this spec is the target.

## Hard WebFX constraints (from earlier findings — obey these)
- **No CSS engine.** `setStyle("-fx-...")` is INERT. Style ONLY via property APIs: `setBackground(new Background(new BackgroundFill(Color.web(hex), new CornerRadii(r), Insets.EMPTY)))`, `setBorder(...)`, `setTextFill(Color)`, `setFont(Font.font(...))`, `setPadding(Insets)`, `setAlignment`, `setSpacing`. (The `Styles` helper already does this correctly — extend it.)
- **No `ComboBox`, `TableView`, `ListView`, `TreeView`.** Use `Button`+`ContextMenu` for dropdowns; build the "table" from `VBox`/`HBox`/`GridPane` rows; scroll via `ScrollPane`.
- **No `java.time`, no Jackson, no `java.net.http`** — already handled in the model/client; don't reintroduce.
- Colours: `PURPLE #840b9b`, `DELETE_RED #db1818`, panel `#f1f1f1`, row light `#efefef` / darker `#e4e4e4`, header grey `#dcdcdc`, text `#333`, border `#cccccc`. Section-heading icons: use emoji (🎁 📦 💰 📐 🏷️) as an approximation.

## Flow change (important): read-only browsing, login is edit-only
Today the app GATES the list behind login. Change it: **ServerSelect → item list loads immediately, read-only.** Login is optional and only unlocks editing. So:
- After ServerSelect/connect, go straight to the **list** (no forced login).
- Title-bar shows a **"Logg inn"** button when logged out; clicking it opens a **login modal** (see below). When logged in, the bar shows the username + a **"Logg ut"** button.
- Edit affordances (**"Legg til produkt"** button in the toolbar; the detail view's Rediger/Lagre/Slett) appear/work **only when logged in**.

## 1. Title bar (AppShell) — persistent, purple `#840b9b`, ~64px tall
- Left: a small **white warehouse glyph** (approximate: a `Label` with 🏭, or a simple white `SVGPath`) + **"Warehouse"** in white **bold ~24px**.
- Right (logged out): a small rounded dark-purple pill with a 👤🔒-style label, then a **white rounded "Logg inn" button** with **purple** text.
- Right (logged in): a pill showing 👤 + the **username** (e.g. "eik") in white, then a **white "Logg ut" button** with purple text.
- Use an `HBox` with a spacer (`Region` + `HBox.setHgrow(spacer, ALWAYS)`) between left and right.

## 2. List view (`ItemListView`)
**Toolbar** (a light `#f1f1f1` rounded bar, padding ~10, spacing ~10, `HBox`):
- **Search**: a `TextField` prompt "Søk…", ~320px wide, with a 🔍 affordance; filters the list live by name/brand (case-insensitive `contains`).
- **Sort "dropdown"**: a `Button` labelled "Sorter ▾" styled white/bordered/rounded; on click show a `ContextMenu` with items **Navn**, **Antall**, **Merke**; selecting sets the sort key. (This replaces the unsupported ComboBox.)
- **Direction toggle**: a small square bordered button showing stacked ▲▼; toggles ascending/descending, re-render.
- **"Legg til produkt"**: purple `Styles.primary` button — **only added when logged in** — opens the detail view on a new blank `Item`.

**Table** (inside a `ScrollPane`, `setFitToWidth(true)`):
- **Header row** (grey `#dcdcdc` background, padding): left column two small stacked labels "**Merke**" / "**Navn**"; right, "**Antall**" (right-aligned). Use a `BorderPane` or `HBox` per row: left `VBox`, right `Label`.
- **Item rows**: a clickable `HBox`/`BorderPane`, **alternating backgrounds** (`#efefef` / `#e4e4e4`... actually light: `#f4f4f4` / white), padding ~10. Left: a `VBox` with **brand** (`Merke`) in normal grey `#555` on top and **name** (`Navn`) in **bold** `#222` below. Right: **amount** (`Antall`) **bold**, right-aligned. Whole row cursor=HAND; on click → open `ItemDetailView` for that item. (Two lines per row — NOT the current single "Name — qty (brand)" line.)

## 3. Login modal (`ItemListView` overlay or a `LoginModal` component)
Make the app root a `StackPane` in `AppShell` so a modal overlay can sit on top. The login is a **centered white rounded card** over a translucent dark backdrop (`Color.rgb(0,0,0,0.35)` fill on a full-size `Region`):
- Heading **"Logg inn"** (bold ~24, dark).
- `TextField` (prompt "Brukernavn"), `PasswordField` (prompt "Passord").
- Purple **"Logg inn"** button → calls `session.server().login(...)`; on success set session auth, close modal, refresh the list (now showing edit affordances).
- Purple **"Registrer ny bruker"** button → swap the card to a register form (username + password + confirm; calls `register`, then back to login).
- Dismiss on backdrop click. Show an inline red error `Label` on failure.

## 4. Detail view (`ItemDetailView`) — the sectioned form (highest priority for fidelity)
A full scrollable view (replaces the list; a "Tilbake" back-arrow returns to the list). Header + sections, each section = an emoji icon + **bold heading**, then fields laid out with `GridPane`/`HBox`. Two modes:
- **View mode** (default): fields **non-editable** (render as bold values or disabled fields); a full-width purple **"✏ Rediger"** button at the very top. Clicking Rediger requires login — if not logged in, open the login modal; if logged in, switch to edit mode.
- **Edit mode**: top shows two buttons side by side — purple **"💾 LAGRE"** and red **"🗑 SLETT"**; fields become editable.

Sections and fields (Norwegian labels, exactly):
- **🎁 Produktinfo**: `Produktnavn` (name) and `Produsent` (brand), two fields side by side.
- **📦 Lagerbeholdning**:
  - **Antall på lager**: a **stepper** — a round grey **−** button, the amount value in a bordered box, a round grey **+** button (−/+ adjust the amount).
  - **Plassering på lager**: **three rounded bordered boxes** side by side, each showing the value **big/centered** with a small caption below — **Seksjon** (item.section), **Reol** (item.row), **Hylle** (item.shelf).
- **💰 Prisdata**: three fields in a row — **Ordinær** (regularPrice, caption "ink.mva"), **Utsalg** (salePrice, "ink.mva"), **Innkjøp** (purchasePrice, "eks.mva").
- **📐 Dimensjoner**: four fields — **Lengde** (length, "cm"), **Bredde** (width, "cm"), **Høyde** (height, "cm"), **Vekt** (weight, "kg").
- **🏷️ Barcode (13 sifre)**: a `TextField` for the 13-digit barcode, and beside it a **rendered barcode image** drawn from the digits onto a `Canvas` (vertical black bars; a reasonable EAN-13-style bar pattern is fine — exact encoding not required, but it must visibly render bars when a code is present, and be blank when empty).

Save (LAGRE) → `session.server().putItem(item, auth)` then back to list; Slett (SLETT) → `removeItem` then back to list; both require `session.getAuth()`.

## Build + deploy (same as before)
- `nix-shell -p jdk17 maven git --run "cd /Users/eik/code/warehouse/webfx-spike && java -jar <webfx-cli-fat.jar> update && java -jar <same> build --gwt 2>&1 | grep -viE 'Progress|Downloading|Downloaded' | tail -25"` → **BUILD SUCCESS**.
- Commit on `webfx-spike`, push.
- Redeploy: `rsync -az --delete -e "/usr/bin/ssh -o BatchMode=yes" /Users/eik/code/warehouse/webfx-spike/warehouse-app-gwt/target/warehouse-app-gwt-1.0.0-SNAPSHOT/warehouse_app_gwt/ eik-desktop:/home/eik/server-infra/data/warehouse-webfx/`
- You can't see the result; the parent agent verifies visually (headless Chrome) and will send precise corrections.
