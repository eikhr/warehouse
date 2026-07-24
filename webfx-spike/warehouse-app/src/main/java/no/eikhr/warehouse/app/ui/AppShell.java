package no.eikhr.warehouse.app.ui;

import javafx.scene.layout.BorderPane;

/** Root container; swaps the center node to navigate between views. */
public class AppShell {
    private final BorderPane root = new BorderPane();

    public BorderPane getRoot() { return root; }

    public void show(View view) { root.setCenter(view.getRoot()); }
}
