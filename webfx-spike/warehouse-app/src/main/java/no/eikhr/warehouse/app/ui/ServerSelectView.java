package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Port of {@code ServerSelect.fxml}: choose the server URL, then log in. */
public class ServerSelectView implements View {
    private final VBox root = new VBox();

    public ServerSelectView(Session session, AppShell shell) {
        TextField url = Styles.input(new TextField(session.getBaseUrl()));
        url.setPromptText("Server-URL");
        url.setPrefWidth(320);
        Button connect = Styles.primary("Koble til");
        connect.setPrefWidth(320);
        Label error = Styles.error("");

        connect.setOnAction(e -> {
            String u = url.getText() == null ? "" : url.getText().trim();
            if (u.isEmpty()) {
                error.setText("Du må velge en server-URL.");
                return;
            }
            session.setBaseUrl(u);
            shell.show(new LoginView(session, shell));
        });

        VBox card = new VBox(14, Styles.heading("Koble til server"),
            Styles.fieldLabel("Server-URL:"), url, connect, error);
        Styles.panel(card);
        card.setMaxWidth(380);

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 20, 20));
        root.getChildren().add(card);
    }

    @Override public Node getRoot() { return root; }
}
