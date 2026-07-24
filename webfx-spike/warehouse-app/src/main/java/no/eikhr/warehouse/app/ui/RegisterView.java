package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.User;
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

/** Port of {@code Register.fxml}: create a user (plaintext password; the server hashes). */
public class RegisterView implements View {
    private final VBox root = new VBox();

    public RegisterView(Session session, AppShell shell) {
        TextField user = Styles.input(new TextField());
        user.setPromptText("Brukernavn");
        user.setPrefWidth(320);
        PasswordField pass1 = Styles.input(new PasswordField());
        pass1.setPromptText("Passord");
        pass1.setPrefWidth(320);
        PasswordField pass2 = Styles.input(new PasswordField());
        pass2.setPromptText("Gjenta passord");
        pass2.setPrefWidth(320);
        Button register = Styles.primary("Registrer");
        register.setPrefWidth(320);
        Hyperlink toLogin = new Hyperlink("Tilbake til innlogging");
        toLogin.setStyle("-fx-text-fill: " + Styles.PURPLE + ";");
        Label error = Styles.error("");

        register.setOnAction(e -> {
            if (user.getText().isEmpty() || pass1.getText().isEmpty() || pass2.getText().isEmpty()) {
                error.setText("Fyll ut alle feltene.");
                return;
            }
            if (!pass1.getText().equals(pass2.getText())) {
                error.setText("Passordene er ikke like.");
                return;
            }
            error.setText("Registrerer…");
            register.setDisable(true);
            session.server().register(new User(genId(), user.getText(), pass1.getText()))
                .onFailure(err -> {
                    register.setDisable(false);
                    error.setText("Registrering feilet: " + err.getMessage());
                })
                .onSuccess(v -> {
                    register.setDisable(false);
                    shell.show(new LoginView(session, shell));
                });
        });
        toLogin.setOnAction(e -> shell.show(new LoginView(session, shell)));

        VBox card = new VBox(12, Styles.heading("Opprett bruker"), user, pass1, pass2, register, toLogin, error);
        Styles.panel(card);
        card.setMaxWidth(380);

        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 20, 20, 20));
        root.getChildren().add(card);
    }

    static String genId() {
        return "web-" + Long.toHexString(System.currentTimeMillis())
            + "-" + Integer.toHexString((int) (Math.random() * 0x7fffffff));
    }

    @Override public Node getRoot() { return root; }
}
