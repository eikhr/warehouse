package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Port of {@code Login.fxml}: username + password, then the item list. */
public class LoginView implements View {
    private final VBox root = new VBox(10);

    public LoginView(Session session, AppShell shell) {
        Label heading = new Label("Log in");
        heading.getStyleClass().add("heading");
        TextField user = new TextField();
        user.setPromptText("username");
        PasswordField pass = new PasswordField();
        pass.setPromptText("password");
        Button login = new Button("Log in");
        Hyperlink toRegister = new Hyperlink("Create account");
        Label error = new Label();

        login.setOnAction(e -> {
            if (user.getText().isEmpty() || pass.getText().isEmpty()) {
                error.setText("Fill in both fields.");
                return;
            }
            error.setText("Logging in…");
            login.setDisable(true);
            session.server().login(new LoginRequest(user.getText(), pass.getText()))
                .onFailure(err -> {
                    login.setDisable(false);
                    error.setText("Login failed: " + err.getMessage());
                })
                .onSuccess(auth -> {
                    login.setDisable(false);
                    if (auth == null || auth.getToken() == null) {
                        error.setText("Login failed: invalid credentials");
                        return;
                    }
                    session.setAuth(auth);
                    shell.show(new ItemListView(session, shell));
                });
        });
        toRegister.setOnAction(e -> shell.show(new RegisterView(session, shell)));

        root.setPadding(new Insets(20));
        root.getChildren().addAll(heading, user, pass, login, toRegister, error);
    }

    @Override public Node getRoot() { return root; }
}
