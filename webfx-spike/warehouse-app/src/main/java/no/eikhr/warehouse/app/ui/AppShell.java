package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.Item;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Root container. A {@link StackPane} so login modals can overlay the app. Layer
 * 0 is a {@link BorderPane} with a persistent purple title bar on top and a
 * swappable center. Navigation goes through the {@code show*} methods so
 * {@link #refresh()} can rebuild the current view after a login state change.
 */
public class AppShell {
    private final StackPane root = new StackPane();
    private final BorderPane frame = new BorderPane();
    private final Session session;
    private Runnable current = () -> {};

    public AppShell(Session session) {
        this.session = session;
        Styles.bg(frame, Color.WHITE, 0);
        root.getChildren().add(frame);
        rebuildTitleBar();
    }

    public StackPane getRoot() { return root; }

    // --- navigation ---
    public void showServerSelect() {
        current = this::showServerSelect;
        frame.setCenter(new ServerSelectView(session, this).getRoot());
    }

    public void showList() {
        current = this::showList;
        rebuildTitleBar();
        frame.setCenter(new ItemListView(session, this).getRoot());
    }

    public void showDetail(Item item) {
        current = () -> showDetail(item);
        rebuildTitleBar();
        frame.setCenter(new ItemDetailView(session, this, item).getRoot());
    }

    /** Rebuild the current center view + title bar (e.g. after login/logout). */
    public void refresh() {
        rebuildTitleBar();
        current.run();
    }

    // --- modal overlay ---
    void showOverlay(Node overlay) { root.getChildren().add(overlay); }
    void hideOverlay(Node overlay) { root.getChildren().remove(overlay); }

    /** Open the login modal; {@code afterLogin} runs on success (title bar already refreshed). */
    public void openLoginModal(Runnable afterLogin) {
        new LoginModal(session, this, () -> { rebuildTitleBar(); afterLogin.run(); }).open();
    }

    // --- title bar ---
    private void rebuildTitleBar() {
        HBox left = new HBox(Styles.icon("logo-white.png", 40));
        left.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(10);
        right.setAlignment(Pos.CENTER_RIGHT);
        if (session.isLoggedIn()) {
            right.getChildren().addAll(pill("user-edit-white.png", session.username()), logoutButton());
        } else {
            right.getChildren().addAll(pill("user-lock-white.png", null), loginButton());
        }

        HBox bar = new HBox(left, spacer, right);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 0, 20));
        bar.setMinHeight(64);
        bar.setPrefHeight(64);
        Styles.bg(bar, Styles.PURPLE, 0);
        frame.setTop(bar);
    }

    private HBox pill(String iconFile, String text) {
        HBox h = new HBox(6, Styles.icon(iconFile, 16));
        if (text != null && !text.isEmpty())
            h.getChildren().add(Styles.label(text, 13, FontWeight.BOLD, Color.WHITE));
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(5, 12, 5, 12));
        Styles.bg(h, Styles.PURPLE_DARK, 14);
        return h;
    }

    private Button loginButton() {
        Button b = Styles.ghost("Logg inn");
        b.setOnAction(e -> openLoginModal(this::refresh));
        return b;
    }

    private Button logoutButton() {
        Button b = Styles.ghost("Logg ut");
        b.setOnAction(e -> { session.setAuth(null); refresh(); });
        return b;
    }
}
