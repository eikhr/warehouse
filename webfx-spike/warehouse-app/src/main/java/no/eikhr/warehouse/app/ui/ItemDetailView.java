package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Sectioned detail form matching the original desktop DetailsView. Two modes:
 * <b>view</b> (read-only, top button "✏ Rediger") and <b>edit</b> (top buttons
 * "💾 LAGRE" + "🗑 SLETT", fields editable). Sections: 🎁 Produktinfo,
 * 📦 Lagerbeholdning (amount stepper + three location boxes), 💰 Prisdata,
 * 📐 Dimensjoner, 🏷️ Barcode (a field + a barcode drawn on a {@link Canvas}).
 * Clicking Rediger while logged out opens the login modal.
 */
public class ItemDetailView implements View {
    private static final String DEFAULT_CREATION_DATE = "2022-01-01T00:00:00.000000000";

    private final VBox root = new VBox();
    private final Session session;
    private final AppShell shell;
    private final Item item;
    private boolean editing;

    private final TextField nameField = new TextField();
    private final TextField brandField = new TextField();
    private final TextField amountField = new TextField();
    private final TextField sectionField = new TextField();
    private final TextField rowField = new TextField();
    private final TextField shelfField = new TextField();
    private final TextField regularField = new TextField();
    private final TextField saleField = new TextField();
    private final TextField purchaseField = new TextField();
    private final TextField lengthField = new TextField();
    private final TextField widthField = new TextField();
    private final TextField heightField = new TextField();
    private final TextField weightField = new TextField();
    private final TextField barcodeField = new TextField();
    private final Canvas barcodeCanvas = new Canvas(250, 80);

    private final Button minusBtn = Styles.round("−");
    private final Button plusBtn = Styles.round("+");
    private final VBox actionArea = new VBox(10);
    private final Label error = Styles.error("");

    private final TextField[] fields = {
        nameField, brandField, amountField, sectionField, rowField, shelfField,
        regularField, saleField, purchaseField, lengthField, widthField, heightField,
        weightField, barcodeField
    };

    public ItemDetailView(Session session, AppShell shell, Item item) {
        this.session = session;
        this.shell = shell;
        this.item = item;
        this.editing = item.getId() == null; // new item starts in edit mode

        populate();

        Button back = Styles.secondary("←  Tilbake");
        back.setOnAction(e -> shell.showList());
        HBox topRow = new HBox(back);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(16,
            topRow,
            actionArea,
            productInfoSection(),
            stockSection(),
            priceSection(),
            dimensionSection(),
            barcodeSection(),
            error);
        content.setPadding(new Insets(20));
        content.setMaxWidth(660);

        VBox centerWrap = new VBox(content);
        centerWrap.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(centerWrap);
        scroll.setFitToWidth(true);
        Styles.bg(scroll, Styles.SCROLL_BG, 0);
        root.getChildren().add(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        barcodeField.textProperty().addListener((o, a, b) -> drawBarcode(b));
        applyMode();
        drawBarcode(barcodeField.getText());
    }

    private void populate() {
        nameField.setText(nz(item.getName()));
        brandField.setText(nz(item.getBrand()));
        amountField.setText(Integer.toString(item.getAmount()));
        sectionField.setText(nz(item.getSection()));
        rowField.setText(nz(item.getRow()));
        shelfField.setText(nz(item.getShelf()));
        regularField.setText(str(item.getRegularPrice()));
        saleField.setText(str(item.getSalePrice()));
        purchaseField.setText(str(item.getPurchasePrice()));
        lengthField.setText(str(item.getLength()));
        widthField.setText(str(item.getWidth()));
        heightField.setText(str(item.getHeight()));
        weightField.setText(str(item.getWeight()));
        barcodeField.setText(nz(item.getBarcode()));
    }

    // ---- sections ----
    private Node productInfoSection() {
        HBox row = new HBox(16, hgrow(fieldGroup("Produktnavn", nameField)),
                                hgrow(fieldGroup("Produsent", brandField)));
        return section("🎁  Produktinfo", row);
    }

    private Node stockSection() {
        Styles.input(amountField);
        amountField.setAlignment(Pos.CENTER);
        amountField.setFont(Font.font("System", FontWeight.BOLD, 16));
        amountField.setPrefWidth(90);
        amountField.setMaxWidth(90);
        HBox stepper = new HBox(10, minusBtn, amountField, plusBtn);
        stepper.setAlignment(Pos.CENTER_LEFT);
        minusBtn.setOnAction(e -> amountField.setText(Integer.toString(Math.max(0, intOf(amountField.getText()) - 1))));
        plusBtn.setOnAction(e -> amountField.setText(Integer.toString(intOf(amountField.getText()) + 1)));

        VBox amountBlock = new VBox(6, Styles.fieldLabel("Antall på lager"), stepper);

        HBox boxes = new HBox(12, hgrow(locationBox("Seksjon", sectionField)),
                                  hgrow(locationBox("Reol", rowField)),
                                  hgrow(locationBox("Hylle", shelfField)));
        VBox placement = new VBox(6, Styles.fieldLabel("Plassering på lager"), boxes);

        return section("📦  Lagerbeholdning", new VBox(16, amountBlock, placement));
    }

    private Node priceSection() {
        HBox row = new HBox(16,
            hgrow(fieldGroupCap("Ordinær", regularField, "ink.mva")),
            hgrow(fieldGroupCap("Utsalg", saleField, "ink.mva")),
            hgrow(fieldGroupCap("Innkjøp", purchaseField, "eks.mva")));
        return section("💰  Prisdata", row);
    }

    private Node dimensionSection() {
        HBox row = new HBox(16,
            hgrow(fieldGroupCap("Lengde", lengthField, "cm")),
            hgrow(fieldGroupCap("Bredde", widthField, "cm")),
            hgrow(fieldGroupCap("Høyde", heightField, "cm")),
            hgrow(fieldGroupCap("Vekt", weightField, "kg")));
        return section("📐  Dimensjoner", row);
    }

    private Node barcodeSection() {
        Styles.input(barcodeField);
        barcodeField.setPromptText("13 sifre");
        barcodeField.setPrefWidth(180);
        VBox left = new VBox(4, Styles.fieldLabel("Strekkode"), barcodeField);
        VBox canvasWrap = new VBox(barcodeCanvas);
        Styles.whiteCard(canvasWrap, 6, 6);
        HBox row = new HBox(16, left, canvasWrap);
        row.setAlignment(Pos.CENTER_LEFT);
        return section("🏷️  Barcode (13 sifre)", row);
    }

    private Node section(String heading, Node body) {
        VBox v = new VBox(12, Styles.sectionHeading(heading), body);
        Styles.panel(v);
        return v;
    }

    // ---- field builders ----
    private static VBox fieldGroup(String label, TextField field) {
        Styles.input(field);
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, Styles.fieldLabel(label), field);
    }

    private static VBox fieldGroupCap(String label, TextField field, String cap) {
        Styles.input(field);
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, Styles.fieldLabel(label), field, Styles.caption(cap));
    }

    private VBox locationBox(String caption, TextField field) {
        field.setAlignment(Pos.CENTER);
        field.setFont(Font.font("System", FontWeight.BOLD, 18));
        field.setMaxWidth(Double.MAX_VALUE);
        Styles.bg(field, Color.TRANSPARENT, 0);
        Styles.border(field, Color.TRANSPARENT, 0, 0);
        Label cap = Styles.label(caption, 12, FontWeight.BOLD, Styles.GREY_TEXT);
        cap.setMaxWidth(Double.MAX_VALUE);
        cap.setAlignment(Pos.CENTER);
        VBox box = new VBox(6, field, cap);
        box.setAlignment(Pos.CENTER);
        Styles.whiteCard(box, 12, 10);
        return box;
    }

    private static Region hgrow(Region r) {
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ---- modes ----
    private void applyMode() {
        for (TextField f : fields) f.setEditable(editing);
        minusBtn.setDisable(!editing);
        plusBtn.setDisable(!editing);
        renderActions();
    }

    private void setEditing(boolean b) { editing = b; applyMode(); }

    private void renderActions() {
        actionArea.getChildren().clear();
        if (editing) {
            Button save = Styles.primary("💾  LAGRE");
            save.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(save, Priority.ALWAYS);
            save.setOnAction(e -> doSave(save));
            Button del = Styles.danger("🗑  SLETT");
            del.setDisable(item.getId() == null);
            del.setOnAction(e -> doDelete(del));
            HBox row = new HBox(10, save, del);
            actionArea.getChildren().add(row);
        } else {
            Button edit = Styles.primary("✏  Rediger");
            edit.setMaxWidth(Double.MAX_VALUE);
            edit.setOnAction(e -> {
                if (session.isLoggedIn()) setEditing(true);
                else shell.openLoginModal(() -> setEditing(true));
            });
            actionArea.getChildren().add(edit);
        }
    }

    // ---- actions ----
    private void doSave(Button save) {
        if (!session.isLoggedIn()) { error.setText("Logg inn for å lagre."); return; }
        if (nameField.getText().trim().isEmpty()) { error.setText("Produktnavn må fylles ut."); return; }

        Double regular, sale, purchase, length, width, height, weight;
        try {
            regular = parseDouble(regularField.getText());
            sale = parseDouble(saleField.getText());
            purchase = parseDouble(purchaseField.getText());
            length = parseDouble(lengthField.getText());
            width = parseDouble(widthField.getText());
            height = parseDouble(heightField.getText());
            weight = parseDouble(weightField.getText());
        } catch (NumberFormatException nfe) {
            error.setText("Priser og dimensjoner må være tall.");
            return;
        }

        item.setName(nameField.getText().trim());
        item.setBrand(emptyToNull(brandField.getText()));
        item.setAmount(intOf(amountField.getText()));
        item.setSection(emptyToNull(sectionField.getText()));
        item.setRow(emptyToNull(rowField.getText()));
        item.setShelf(emptyToNull(shelfField.getText()));
        item.setRegularPrice(regular);
        item.setSalePrice(sale);
        item.setPurchasePrice(purchase);
        item.setLength(length);
        item.setWidth(width);
        item.setHeight(height);
        item.setWeight(weight);
        item.setBarcode(emptyToNull(barcodeField.getText()));
        if (item.getId() == null) item.setId(Ids.newId());
        if (item.getCreationDate() == null) item.setCreationDate(DEFAULT_CREATION_DATE);

        error.setText("Lagrer…");
        save.setDisable(true);
        session.server().putItem(item, session.getAuth())
            .onFailure(err -> { save.setDisable(false); error.setText("Lagring feilet: " + err.getMessage()); })
            .onSuccess(v -> shell.showList());
    }

    private void doDelete(Button del) {
        if (!session.isLoggedIn()) { error.setText("Logg inn for å slette."); return; }
        if (item.getId() == null) { error.setText("Ingenting å slette."); return; }
        error.setText("Sletter…");
        del.setDisable(true);
        session.server().removeItem(item.getId(), session.getAuth())
            .onFailure(err -> { del.setDisable(false); error.setText("Sletting feilet: " + err.getMessage()); })
            .onSuccess(v -> shell.showList());
    }

    // ---- barcode drawing ----
    private void drawBarcode(String code) {
        GraphicsContext g = barcodeCanvas.getGraphicsContext2D();
        double w = barcodeCanvas.getWidth(), h = barcodeCanvas.getHeight();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        StringBuilder digits = new StringBuilder();
        if (code != null)
            for (int i = 0; i < code.length() && digits.length() < 13; i++) {
                char c = code.charAt(i);
                if (c >= '0' && c <= '9') digits.append(c);
            }
        if (digits.length() == 0) return;

        String[] L = {"0001101", "0011001", "0010011", "0111101", "0100011",
                      "0110001", "0101111", "0111011", "0110111", "0001011"};
        StringBuilder mods = new StringBuilder("101");
        for (int i = 0; i < digits.length(); i++)
            mods.append(L[digits.charAt(i) - '0']);
        mods.append("101");

        int n = mods.length();
        double margin = 10;
        double unit = (w - 2 * margin) / n;
        double top = 8, barH = h - 26;
        g.setFill(Color.BLACK);
        double x = margin;
        for (int i = 0; i < n; i++) {
            if (mods.charAt(i) == '1') g.fillRect(x, top, unit + 0.4, barH);
            x += unit;
        }
        g.setFont(Font.font("System", 10));
        g.fillText(digits.toString(), margin, h - 5);
    }

    // ---- helpers ----
    private static String nz(String s) { return s == null ? "" : s; }
    private static String str(Double d) { return d == null ? "" : d.toString(); }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static int intOf(String s) {
        try {
            return (s == null || s.trim().isEmpty()) ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return Double.valueOf(s.trim());
    }

    @Override public Node getRoot() { return root; }
}
