package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Port of {@code Warehouse.fxml}/{@code WarehouseController}. Fetches items and
 * renders each as a clickable row. The FXML {@code ComboBox} sort selector is
 * replaced by a {@link ToggleGroup} of {@link RadioButton}s (WebFX-supported).
 */
public class ItemListView implements View {
    private final BorderPane root = new BorderPane();
    private final VBox rows = new VBox(4);
    private final Label status = new Label();
    private final Session session;
    private final AppShell shell;
    private List<Item> items = Collections.emptyList();
    private Comparator<Item> currentSort = byName();

    public ItemListView(Session session, AppShell shell) {
        this.session = session;
        this.shell = shell;

        ToggleGroup sort = new ToggleGroup();
        RadioButton byName = new RadioButton("Name");   byName.setToggleGroup(sort); byName.setSelected(true);
        RadioButton byAmount = new RadioButton("Amount"); byAmount.setToggleGroup(sort);
        RadioButton byBrand = new RadioButton("Brand");  byBrand.setToggleGroup(sort);
        byName.setOnAction(e -> { currentSort = byName(); render(); });
        byAmount.setOnAction(e -> { currentSort = Comparator.comparingInt(Item::getAmount); render(); });
        byBrand.setOnAction(e -> { currentSort = byBrand(); render(); });

        Button add = new Button("Add item");
        add.setOnAction(e -> shell.show(new ItemDetailView(session, shell, new Item())));

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> load());

        String who = session.username();
        Label user = new Label(who != null ? "Signed in as " + who : "Not signed in");
        Hyperlink logout = new Hyperlink("Log out");
        logout.setOnAction(e -> { session.setAuth(null); shell.show(new LoginView(session, shell)); });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox userBar = new HBox(10, user, spacer, logout);
        userBar.setPadding(new Insets(10, 10, 0, 10));

        HBox top = new HBox(10, new Label("Sort:"), byName, byAmount, byBrand, add, refresh);
        top.setPadding(new Insets(10));

        rows.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);

        root.setTop(new VBox(userBar, top, status));
        root.setCenter(scroll);
        status.setPadding(new Insets(0, 10, 6, 10));

        load();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static Comparator<Item> byName() { return Comparator.comparing((Item i) -> nz(i.getName()), String.CASE_INSENSITIVE_ORDER); }
    private static Comparator<Item> byBrand() { return Comparator.comparing((Item i) -> nz(i.getBrand()), String.CASE_INSENSITIVE_ORDER); }

    private void load() {
        status.setText("Loading…");
        rows.getChildren().clear();
        session.server().getItems()
            .onFailure(err -> status.setText("Error: " + err.getMessage()))
            .onSuccess(list -> {
                items = list;
                status.setText(list.size() + " items");
                render();
            });
    }

    private void render() {
        items.sort(currentSort);
        rows.getChildren().clear();
        for (Item it : items) {
            String label = nz(it.getName()) + "  —  qty " + it.getAmount()
                + (nz(it.getBrand()).isEmpty() ? "" : "  (" + it.getBrand() + ")");
            Hyperlink row = new Hyperlink(label);
            row.setOnAction(e -> shell.show(new ItemDetailView(session, shell, it)));
            rows.getChildren().add(row);
        }
    }

    @Override public Node getRoot() { return root; }
}
