package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Port of {@code Login.fxml}: username + password, then the item list. */
public class LoginView implements View {
    private final VBox root = new VBox();

    public LoginView(Session session, AppShell shell) {
        TextField user = Styles.input(new TextField());
        user.setPromptText("Brukernavn");
        user.setPrefWidth(320);
        PasswordField pass = Styles.input(new PasswordField());
        pass.setPromptText("Passord");
        pass.setPrefWidth(320);
        Button login = Styles.primary("Logg inn");
        login.setPrefWidth(320);
        Hyperlink toRegister = new Hyperlink("Opprett bruker");
        toRegister.setStyle("-fx-text-fill: " + Styles.PURPLE + ";");
        Label error = Styles.error("");

        login.setOnAction(e -> {
            if (user.getText().isEmpty() || pass.getText().isEmpty()) {
                error.setText("Fyll ut begge feltene.");
                return;
            }
            error.setText("Logger inn…");
            login.setDisable(true);
            session.server().login(new LoginRequest(user.getText(), pass.getText()))
                .onFailure(err -> {
                    login.setDisable(false);
                    error.setText("Innlogging feilet: " + err.getMessage());
                })
                .onSuccess(auth -> {
                    login.setDisable(false);
                    if (auth == null || auth.getToken() == null) {
                        error.setText("Innlogging feilet: feil brukernavn eller passord");
                        return;
                    }
                    session.setAuth(auth);
                    shell.show(new ItemListView(session, shell));
                });
        });
        toRegister.setOnAction(e -> shell.show(new RegisterView(session, shell)));

        VBox card = new VBox(12, Styles.heading("Logg inn"), user, pass, login, toRegister, error);
        Styles.panel(card);
        card.setMaxWidth(380);

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 20, 20));
        root.getChildren().add(card);
    }

    @Override public Node getRoot() { return root; }
}
