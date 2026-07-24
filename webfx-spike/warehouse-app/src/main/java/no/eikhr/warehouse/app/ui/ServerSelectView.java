package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Port of {@code ServerSelect.fxml}: choose the server URL, then log in. */
public class ServerSelectView implements View {
    private final VBox root = new VBox(10);

    public ServerSelectView(Session session, AppShell shell) {
        Label heading = new Label("Connect to server");
        heading.getStyleClass().add("heading");
        TextField url = new TextField(session.getBaseUrl());
        Button connect = new Button("Connect");
        Label error = new Label();
        connect.setOnAction(e -> {
            String u = url.getText() == null ? "" : url.getText().trim();
            if (u.isEmpty()) {
                error.setText("Please enter a server URL.");
                return;
            }
            session.setBaseUrl(u);
            shell.show(new LoginView(session, shell));
        });
        root.setPadding(new Insets(20));
        root.getChildren().addAll(heading, new Label("Server URL:"), url, connect, error);
    }

    @Override public Node getRoot() { return root; }
}
