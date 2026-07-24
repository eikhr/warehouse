package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Port of {@code Warehouse.fxml}/{@code WarehouseController}. Fetches items and
 * renders each as a clickable styled row. The FXML {@code ComboBox} sort selector
 * is replaced by a {@link ToggleGroup} of {@link RadioButton}s, and a search field
 * filters the list (client-side).
 */
public class ItemListView implements View {
    private final BorderPane root = new BorderPane();
    private final VBox rows = new VBox(6);
    private final Label status = new Label();
    private final TextField search = Styles.input(new TextField());
    private final Session session;
    private final AppShell shell;
    private List<Item> allItems = Collections.emptyList();
    private Comparator<Item> currentSort = byName();

    public ItemListView(Session session, AppShell shell) {
        this.session = session;
        this.shell = shell;

        // --- user bar ---
        String who = session.username();
        Label user = new Label(who != null ? "Logget inn som " + who : "Ikke innlogget");
        user.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));
        user.setTextFill(Styles.PURPLE);
        Button logout = Styles.secondary("Logg ut");
        logout.setOnAction(e -> { session.setAuth(null); shell.show(new LoginView(session, shell)); });
        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        HBox userBar = new HBox(10, user, s1, logout);
        userBar.setAlignment(Pos.CENTER_LEFT);

        // --- search + actions ---
        search.setPromptText("Søk...");
        HBox.setHgrow(search, Priority.ALWAYS);
        search.textProperty().addListener((obs, o, n) -> applyView());
        Button add = Styles.primary("Legg til produkt");
        add.setOnAction(e -> shell.show(new ItemDetailView(session, shell, new Item())));
        Button refresh = Styles.secondary("Oppdater");
        refresh.setOnAction(e -> load());
        HBox actions = new HBox(10, search, add, refresh);
        actions.setAlignment(Pos.CENTER_LEFT);

        // --- sort ---
        ToggleGroup sort = new ToggleGroup();
        RadioButton byName = radio("Navn", sort); byName.setSelected(true);
        RadioButton byAmount = radio("Antall", sort);
        RadioButton byBrand = radio("Produsent", sort);
        byName.setOnAction(e -> { currentSort = byName(); applyView(); });
        byAmount.setOnAction(e -> { currentSort = Comparator.comparingInt(Item::getAmount); applyView(); });
        byBrand.setOnAction(e -> { currentSort = byBrand(); applyView(); });
        Label sortLabel = Styles.fieldLabel("Sorter:");
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        HBox sortBar = new HBox(10, sortLabel, byName, byAmount, byBrand, s2, status);
        sortBar.setAlignment(Pos.CENTER_LEFT);
        status.setTextFill(Styles.TEXT_DARK);

        VBox header = new VBox(12, userBar, actions, sortBar);
        header.setPadding(new Insets(16, 18, 12, 18));

        rows.setPadding(new Insets(6, 18, 18, 18));
        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        Styles.bg(scroll, Styles.SCROLL_BG, 0);

        root.setTop(header);
        root.setCenter(scroll);

        load();
    }

    private static RadioButton radio(String text, ToggleGroup g) {
        RadioButton r = new RadioButton(text);
        r.setToggleGroup(g);
        r.setCursor(Cursor.HAND);
        return r;
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static Comparator<Item> byName() { return Comparator.comparing((Item i) -> nz(i.getName()), String.CASE_INSENSITIVE_ORDER); }
    private static Comparator<Item> byBrand() { return Comparator.comparing((Item i) -> nz(i.getBrand()), String.CASE_INSENSITIVE_ORDER); }

    private void load() {
        status.setText("Laster…");
        rows.getChildren().clear();
        session.server().getItems()
            .onFailure(err -> status.setText("Feil: " + err.getMessage()))
            .onSuccess(list -> { allItems = list; applyView(); });
    }

    private void applyView() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        List<Item> view = new ArrayList<>();
        for (Item it : allItems) {
            if (q.isEmpty()
                || nz(it.getName()).toLowerCase().contains(q)
                || nz(it.getBrand()).toLowerCase().contains(q)) {
                view.add(it);
            }
        }
        view.sort(currentSort);
        status.setText(view.size() + " produkter");
        render(view);
    }

    private void render(List<Item> view) {
        rows.getChildren().clear();
        int idx = 0;
        for (Item it : view) {
            String brand = nz(it.getBrand());
            String text = nz(it.getName()) + "     ·     antall " + it.getAmount()
                + (brand.isEmpty() ? "" : "     ·     " + brand);
            Button row = new Button(text);
            row.setFont(Font.font("System", 14));
            row.setMaxWidth(Double.MAX_VALUE);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setCursor(Cursor.HAND);
            boolean odd = (idx++ % 2) == 1;
            Styles.bg(row, odd ? Styles.ROW_DARK : Styles.ROW_LIGHT, 8);
            row.setTextFill(Styles.TEXT_DARK);
            row.setOnAction(e -> shell.show(new ItemDetailView(session, shell, it)));
            rows.getChildren().add(row);
        }
    }

    @Override public Node getRoot() { return root; }
}
