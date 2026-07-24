package no.eikhr.warehouse.app;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.fetch.json.JsonFetch;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * WebFX spike: a real warehouse view ported to the web.
 *
 * Ports two things from the JavaFX client: the ServerSelect concept (enter a
 * server URL) and the item list. The networking that used java.net.http +
 * Jackson is replaced by WebFX's JsonFetch (Fetch API + AST-JSON) so it can
 * transpile to JS. Same endpoint the desktop client and React app use; the
 * server's @CrossOrigin allows the browser fetch.
 */
public class WarehouseApp extends Application {

    private final VBox itemsBox = new VBox(4);
    private final Label status = new Label();

    @Override
    public void start(Stage stage) {
        TextField urlField = new TextField("https://warehouse.eikhr.no");
        HBox.setHgrow(urlField, Priority.ALWAYS);
        Button loadBtn = new Button("Load items");
        loadBtn.setOnAction(e -> loadItems(urlField.getText().trim()));

        HBox top = new HBox(8, new Label("Server:"), urlField, loadBtn);
        top.setPadding(new Insets(10));

        itemsBox.setPadding(new Insets(10));
        ScrollPane scroll = new ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        status.setPadding(new Insets(0, 10, 6, 10));
        VBox root = new VBox(top, status, scroll);

        stage.setScene(new Scene(root, 820, 620));
        stage.setTitle("Warehouse (WebFX)");
        stage.show();

        loadItems(urlField.getText().trim());
    }

    private void loadItems(String baseUrl) {
        status.setText("Loading from " + baseUrl + "/warehouse/items ...");
        itemsBox.getChildren().clear();
        JsonFetch.fetchJsonArray(baseUrl + "/warehouse/items")
            .onFailure(err -> status.setText("Error: " + err.getMessage()))
            .onSuccess(items -> {
                int n = items.size();
                status.setText(n + " items loaded from " + baseUrl);
                itemsBox.getChildren().clear();
                for (int i = 0; i < n; i++) {
                    ReadOnlyAstObject item = items.getObject(i);
                    String name = item.getString("name", "(no name)");
                    Integer amount = item.getInteger("amount", 0);
                    String brand = item.getString("brand", "");
                    String text = name + "  —  qty " + amount
                            + (brand.isEmpty() ? "" : "  (" + brand + ")");
                    itemsBox.getChildren().add(new Label(text));
                }
            });
    }
}
