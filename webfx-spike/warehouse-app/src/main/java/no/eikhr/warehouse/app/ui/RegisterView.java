package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.User;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Port of {@code Register.fxml}: create a user (plaintext password; the server hashes). */
public class RegisterView implements View {
    private final VBox root = new VBox(10);

    public RegisterView(Session session, AppShell shell) {
        Label heading = new Label("Create account");
        heading.getStyleClass().add("heading");
        TextField user = new TextField();
        user.setPromptText("username");
        PasswordField pass1 = new PasswordField();
        pass1.setPromptText("password");
        PasswordField pass2 = new PasswordField();
        pass2.setPromptText("confirm password");
        Button register = new Button("Register");
        Hyperlink toLogin = new Hyperlink("Back to login");
        Label error = new Label();

        register.setOnAction(e -> {
            if (user.getText().isEmpty() || pass1.getText().isEmpty() || pass2.getText().isEmpty()) {
                error.setText("Fill in all fields.");
                return;
            }
            if (!pass1.getText().equals(pass2.getText())) {
                error.setText("Passwords do not match.");
                return;
            }
            error.setText("Registering…");
            register.setDisable(true);
            session.server().register(new User(genId(), user.getText(), pass1.getText()))
                .onFailure(err -> {
                    register.setDisable(false);
                    error.setText("Registration failed: " + err.getMessage());
                })
                .onSuccess(v -> {
                    register.setDisable(false);
                    shell.show(new LoginView(session, shell));
                });
        });
        toLogin.setOnAction(e -> shell.show(new LoginView(session, shell)));

        root.setPadding(new Insets(20));
        root.getChildren().addAll(heading, user, pass1, pass2, register, toLogin, error);
    }

    static String genId() {
        return "web-" + Long.toHexString(System.currentTimeMillis())
            + "-" + Integer.toHexString((int) (Math.random() * 0x7fffffff));
    }

    @Override public Node getRoot() { return root; }
}
