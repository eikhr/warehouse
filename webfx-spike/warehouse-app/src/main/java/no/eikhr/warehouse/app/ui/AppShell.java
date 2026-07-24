package no.eikhr.warehouse.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/** Root container: a persistent purple title bar on top; the center swaps per view. */
public class AppShell {
    private final BorderPane root = new BorderPane();

    public AppShell() {
        HBox titleBar = new HBox(Styles.title("Warehouse"));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(12, 20, 12, 20));
        titleBar.setStyle("-fx-background-color: " + Styles.PURPLE + ";");
        root.setTop(titleBar);
        root.setStyle("-fx-background-color: white;");
    }

    public BorderPane getRoot() { return root; }

    public void show(View view) { root.setCenter(view.getRoot()); }
}
