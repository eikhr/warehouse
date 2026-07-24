package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Port of {@code DetailsView.fxml}/{@code DetailsViewController}. A form of
 * {@link TextField}s for the item fields, with Lagre ({@code putItem}, auth
 * required), Slett ({@code removeItem}, auth required) and Tilbake.
 *
 * <p>The barcode image (barbecue) is intentionally dropped: {@code barcode} is a
 * plain text field. Dimensions/weight are not edited here but are preserved from
 * the loaded item on save.
 */
public class ItemDetailView implements View {
    private static final String DEFAULT_CREATION_DATE = "2022-01-01T00:00:00.000000000";

    private final VBox root = new VBox();

    private final TextField nameField = Styles.input(new TextField());
    private final TextField amountField = Styles.input(new TextField());
    private final TextField barcodeField = Styles.input(new TextField());
    private final TextField brandField = Styles.input(new TextField());
    private final TextField regularPriceField = Styles.input(new TextField());
    private final TextField salePriceField = Styles.input(new TextField());
    private final TextField purchasePriceField = Styles.input(new TextField());
    private final TextField sectionField = Styles.input(new TextField());
    private final TextField rowField = Styles.input(new TextField());
    private final TextField shelfField = Styles.input(new TextField());

    public ItemDetailView(Session session, AppShell shell, Item item) {
        boolean isNew = item.getId() == null;

        Label heading = Styles.heading(isNew ? "Legg til produkt" : "Rediger produkt");
        Label error = Styles.error("");

        // Populate from the item.
        nameField.setText(nz(item.getName()));
        amountField.setText(Integer.toString(item.getAmount()));
        barcodeField.setText(nz(item.getBarcode()));
        brandField.setText(nz(item.getBrand()));
        regularPriceField.setText(str(item.getRegularPrice()));
        salePriceField.setText(str(item.getSalePrice()));
        purchasePriceField.setText(str(item.getPurchasePrice()));
        sectionField.setText(nz(item.getSection()));
        rowField.setText(nz(item.getRow()));
        shelfField.setText(nz(item.getShelf()));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        int r = 0;
        addRow(grid, r++, "Produktnavn", nameField);
        addRow(grid, r++, "Antall", amountField);
        addRow(grid, r++, "Strekkode", barcodeField);
        addRow(grid, r++, "Produsent", brandField);
        addRow(grid, r++, "Ordinær pris", regularPriceField);
        addRow(grid, r++, "Salgspris", salePriceField);
        addRow(grid, r++, "Innkjøpspris", purchasePriceField);
        addRow(grid, r++, "Seksjon", sectionField);
        addRow(grid, r++, "Rad", rowField);
        addRow(grid, r++, "Hylle", shelfField);
        Styles.panel(grid);

        Button save = Styles.primary("Lagre");
        Button delete = Styles.danger("Slett");
        Button back = Styles.secondary("Tilbake");
        delete.setDisable(isNew);

        save.setOnAction(e -> {
            if (!session.isLoggedIn()) { error.setText("Logg inn for å lagre."); return; }
            if (nameField.getText().trim().isEmpty()) { error.setText("Produktnavn må fylles ut."); return; }

            int amount;
            Double regular, sale, purchase;
            try {
                amount = parseInt(amountField.getText());
                regular = parseDouble(regularPriceField.getText());
                sale = parseDouble(salePriceField.getText());
                purchase = parseDouble(purchasePriceField.getText());
            } catch (NumberFormatException nfe) {
                error.setText("Antall/priser må være tall.");
                return;
            }

            item.setName(nameField.getText().trim());
            item.setAmount(amount);
            item.setBarcode(emptyToNull(barcodeField.getText()));
            item.setBrand(emptyToNull(brandField.getText()));
            item.setRegularPrice(regular);
            item.setSalePrice(sale);
            item.setPurchasePrice(purchase);
            item.setSection(emptyToNull(sectionField.getText()));
            item.setRow(emptyToNull(rowField.getText()));
            item.setShelf(emptyToNull(shelfField.getText()));
            if (item.getId() == null) item.setId(RegisterView.genId());
            if (item.getCreationDate() == null) item.setCreationDate(DEFAULT_CREATION_DATE);

            error.setText("Lagrer…");
            save.setDisable(true);
            session.server().putItem(item, session.getAuth())
                .onFailure(err -> { save.setDisable(false); error.setText("Lagring feilet: " + err.getMessage()); })
                .onSuccess(v -> shell.show(new ItemListView(session, shell)));
        });

        delete.setOnAction(e -> {
            if (!session.isLoggedIn()) { error.setText("Logg inn for å slette."); return; }
            if (item.getId() == null) { error.setText("Ingenting å slette."); return; }
            error.setText("Sletter…");
            delete.setDisable(true);
            session.server().removeItem(item.getId(), session.getAuth())
                .onFailure(err -> { delete.setDisable(false); error.setText("Sletting feilet: " + err.getMessage()); })
                .onSuccess(v -> shell.show(new ItemListView(session, shell)));
        });

        back.setOnAction(e -> shell.show(new ItemListView(session, shell)));

        HBox buttons = new HBox(10, save, delete, back);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(16, heading, grid, buttons, error);
        content.setPadding(new Insets(20));
        content.setMaxWidth(560);

        VBox centerWrap = new VBox(content);
        centerWrap.setAlignment(Pos.TOP_CENTER);
        ScrollPane scroll = new ScrollPane(centerWrap);
        scroll.setFitToWidth(true);
        Styles.bg(scroll, Styles.SCROLL_BG, 0);
        root.getChildren().add(scroll);
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);
    }

    private static void addRow(GridPane grid, int row, String label, TextField field) {
        field.setPrefWidth(260);
        grid.add(Styles.fieldLabel(label), 0, row);
        grid.add(field, 1, row);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static String str(Double d) { return d == null ? "" : d.toString(); }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static int parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Integer.parseInt(s.trim());
    }

    private static Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return Double.valueOf(s.trim());
    }

    @Override public Node getRoot() { return root; }
}
