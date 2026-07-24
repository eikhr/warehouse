package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only browsable item list (no login required). Toolbar has a live search,
 * a "Sorter ▾" menu-button ({@link Button} + {@link ContextMenu}, since WebFX has
 * no ComboBox), a ▲/▼ direction toggle, and — only when logged in — a purple
 * "Legg til produkt" button. The table is built from {@link BorderPane} rows with
 * two lines each (brand grey on top, name bold below) and a bold right-aligned
 * amount, alternating row backgrounds, clickable to the detail view.
 */
public class ItemListView implements View {
    private enum SortKey { NAME, AMOUNT, BRAND }

    private final BorderPane root = new BorderPane();
    private final VBox rows = new VBox();
    private final TextField search = Styles.input(new TextField());
    private final Session session;
    private final AppShell shell;

    private List<Item> allItems = Collections.emptyList();
    private SortKey sortKey = SortKey.NAME;
    private boolean ascending = true;
    private final Button sortBtn = Styles.menuButton("Sorter: Navn ▾");
    private final Button dirBtn = Styles.menuButton("▲");

    public ItemListView(Session session, AppShell shell) {
        this.session = session;
        this.shell = shell;

        root.setTop(buildToolbar());

        ScrollPane scroll = new ScrollPane(new VBox(header(), rows));
        scroll.setFitToWidth(true);
        Styles.bg(scroll, Styles.SCROLL_BG, 0);
        root.setCenter(scroll);

        load();
    }

    // --- toolbar ---
    private Node buildToolbar() {
        search.setPromptText("🔍  Søk…");
        search.setPrefWidth(320);
        search.textProperty().addListener((obs, o, n) -> render());

        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
            sortItem("Navn", SortKey.NAME),
            sortItem("Antall", SortKey.AMOUNT),
            sortItem("Merke", SortKey.BRAND));
        sortBtn.setOnAction(e -> menu.show(sortBtn, Side.BOTTOM, 0, 0));

        dirBtn.setMinWidth(40);
        dirBtn.setOnAction(e -> { ascending = !ascending; dirBtn.setText(ascending ? "▲" : "▼"); render(); });

        HBox bar = new HBox(10, search, sortBtn, dirBtn);
        bar.setAlignment(Pos.CENTER_LEFT);

        if (session.isLoggedIn()) {
            Button add = Styles.primary("Legg til produkt");
            add.setOnAction(e -> shell.showDetail(new Item()));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            bar.getChildren().addAll(spacer, add);
        }

        bar.setPadding(new Insets(10));
        Styles.bg(bar, Styles.PANEL, 10);
        VBox wrap = new VBox(bar);
        wrap.setPadding(new Insets(12, 16, 8, 16));
        return wrap;
    }

    private MenuItem sortItem(String text, SortKey key) {
        MenuItem mi = new MenuItem(text);
        mi.setOnAction(e -> { sortKey = key; sortBtn.setText("Sorter: " + text + " ▾"); render(); });
        return mi;
    }

    // --- table header ---
    private Node header() {
        VBox leftLabels = new VBox(
            Styles.label("Merke", 11, FontWeight.BOLD, Styles.GREY_TEXT),
            Styles.label("Navn", 13, FontWeight.BOLD, Styles.NAME_DARK));
        Label amount = Styles.label("Antall", 13, FontWeight.BOLD, Styles.NAME_DARK);
        BorderPane h = new BorderPane();
        h.setLeft(leftLabels);
        h.setRight(amount);
        BorderPane.setAlignment(amount, Pos.CENTER_RIGHT);
        h.setPadding(new Insets(10, 18, 10, 18));
        Styles.bg(h, Styles.HEADER_GREY, 0);
        return h;
    }

    // --- data ---
    private void load() {
        rows.getChildren().setAll(info("Laster…"));
        session.server().getItems()
            .onFailure(err -> rows.getChildren().setAll(info("Feil: " + err.getMessage())))
            .onSuccess(list -> { allItems = list; render(); });
    }

    private Label info(String text) {
        Label l = Styles.label(text, 13, FontWeight.NORMAL, Styles.GREY_TEXT);
        l.setPadding(new Insets(16));
        return l;
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private Comparator<Item> comparator() {
        Comparator<Item> c;
        switch (sortKey) {
            case AMOUNT: c = Comparator.comparingInt(Item::getAmount); break;
            case BRAND:  c = Comparator.comparing((Item i) -> nz(i.getBrand()), String.CASE_INSENSITIVE_ORDER); break;
            default:     c = Comparator.comparing((Item i) -> nz(i.getName()), String.CASE_INSENSITIVE_ORDER); break;
        }
        return ascending ? c : c.reversed();
    }

    private void render() {
        String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        List<Item> view = new ArrayList<>();
        for (Item it : allItems) {
            if (q.isEmpty()
                || nz(it.getName()).toLowerCase().contains(q)
                || nz(it.getBrand()).toLowerCase().contains(q)) {
                view.add(it);
            }
        }
        view.sort(comparator());

        rows.getChildren().clear();
        if (view.isEmpty()) {
            rows.getChildren().add(info("Ingen produkter."));
            return;
        }
        int idx = 0;
        for (Item it : view) {
            rows.getChildren().add(itemRow(it, (idx++ % 2) == 0));
        }
    }

    private Node itemRow(Item it, boolean even) {
        Label brand = Styles.label(nz(it.getBrand()), 12, FontWeight.NORMAL, Styles.GREY_TEXT);
        Label name = Styles.label(nz(it.getName()), 14, FontWeight.BOLD, Styles.NAME_DARK);
        VBox left = new VBox(2, brand, name);
        Label amount = Styles.label(Integer.toString(it.getAmount()), 14, FontWeight.BOLD, Styles.NAME_DARK);

        BorderPane row = new BorderPane();
        row.setLeft(left);
        row.setRight(amount);
        BorderPane.setAlignment(amount, Pos.CENTER_RIGHT);
        row.setPadding(new Insets(10, 18, 10, 18));
        Styles.bg(row, even ? Styles.ROW_A : Styles.ROW_B, 0);
        row.setCursor(Cursor.HAND);
        row.setOnMousePressed(e -> shell.showDetail(it));
        return row;
    }

    @Override public Node getRoot() { return root; }
}
