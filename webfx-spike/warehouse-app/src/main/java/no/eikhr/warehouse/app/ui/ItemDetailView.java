package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Port of {@code DetailsView.fxml}/{@code DetailsViewController}. A form of
 * {@link TextField}s for the item fields, with Save ({@code putItem}, auth
 * required), Delete ({@code removeItem}, auth required) and Back.
 *
 * <p>The barcode image (barbecue) is intentionally dropped: {@code barcode} is a
 * plain text field. Dimensions/weight are not edited here but are preserved from
 * the loaded item on save.
 */
public class ItemDetailView implements View {
    private static final String DEFAULT_CREATION_DATE = "2022-01-01T00:00:00.000000000";

    private final VBox root = new VBox(12);

    private final TextField nameField = new TextField();
    private final TextField amountField = new TextField();
    private final TextField barcodeField = new TextField();
    private final TextField brandField = new TextField();
    private final TextField regularPriceField = new TextField();
    private final TextField salePriceField = new TextField();
    private final TextField purchasePriceField = new TextField();
    private final TextField sectionField = new TextField();
    private final TextField rowField = new TextField();
    private final TextField shelfField = new TextField();

    public ItemDetailView(Session session, AppShell shell, Item item) {
        boolean isNew = item.getId() == null;

        Label heading = new Label(isNew ? "Add item" : "Edit item");
        heading.getStyleClass().add("heading");
        Label error = new Label();

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
        grid.setHgap(10);
        grid.setVgap(8);
        int r = 0;
        addRow(grid, r++, "Name", nameField);
        addRow(grid, r++, "Amount", amountField);
        addRow(grid, r++, "Barcode", barcodeField);
        addRow(grid, r++, "Brand", brandField);
        addRow(grid, r++, "Regular price", regularPriceField);
        addRow(grid, r++, "Sale price", salePriceField);
        addRow(grid, r++, "Purchase price", purchasePriceField);
        addRow(grid, r++, "Section", sectionField);
        addRow(grid, r++, "Row", rowField);
        addRow(grid, r++, "Shelf", shelfField);

        Button save = new Button("Save");
        Button delete = new Button("Delete");
        Button back = new Button("Back");
        delete.setDisable(isNew);

        save.setOnAction(e -> {
            if (!session.isLoggedIn()) { error.setText("Log in to save."); return; }
            if (nameField.getText().trim().isEmpty()) { error.setText("Name is required."); return; }

            int amount;
            Double regular, sale, purchase;
            try {
                amount = parseInt(amountField.getText());
                regular = parseDouble(regularPriceField.getText());
                sale = parseDouble(salePriceField.getText());
                purchase = parseDouble(purchasePriceField.getText());
            } catch (NumberFormatException nfe) {
                error.setText("Amount/prices must be numbers.");
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

            error.setText("Saving…");
            save.setDisable(true);
            session.server().putItem(item, session.getAuth())
                .onFailure(err -> { save.setDisable(false); error.setText("Save failed: " + err.getMessage()); })
                .onSuccess(v -> shell.show(new ItemListView(session, shell)));
        });

        delete.setOnAction(e -> {
            if (!session.isLoggedIn()) { error.setText("Log in to delete."); return; }
            if (item.getId() == null) { error.setText("Nothing to delete."); return; }
            error.setText("Deleting…");
            delete.setDisable(true);
            session.server().removeItem(item.getId(), session.getAuth())
                .onFailure(err -> { delete.setDisable(false); error.setText("Delete failed: " + err.getMessage()); })
                .onSuccess(v -> shell.show(new ItemListView(session, shell)));
        });

        back.setOnAction(e -> shell.show(new ItemListView(session, shell)));

        HBox buttons = new HBox(10, save, delete, back);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(heading, grid, buttons, error);
    }

    private static void addRow(GridPane grid, int row, String label, TextField field) {
        grid.add(new Label(label), 0, row);
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
