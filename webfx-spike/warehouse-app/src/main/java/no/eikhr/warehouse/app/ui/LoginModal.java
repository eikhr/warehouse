package no.eikhr.warehouse.app.ui;

import no.eikhr.warehouse.app.model.LoginRequest;
import no.eikhr.warehouse.app.model.User;
import no.eikhr.warehouse.app.session.Session;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

/**
 * A centered login/register card over a translucent backdrop, overlaid on the
 * {@link AppShell}'s {@link StackPane}. Login is optional (browsing is read-only);
 * a successful login runs {@code onSuccess} (which refreshes the current view so
 * edit affordances appear). The card sits in a transparent {@link VBox} holder
 * (centered, not stretched); clicks on the backdrop dismiss the modal.
 */
class LoginModal {
    private final Session session;
    private final AppShell shell;
    private final Runnable onSuccess;
    private final StackPane overlay = new StackPane();
    private final VBox cardHolder = new VBox();

    LoginModal(Session session, AppShell shell, Runnable onSuccess) {
        this.session = session;
        this.shell = shell;
        this.onSuccess = onSuccess;

        Region backdrop = new Region();
        Styles.bg(backdrop, Color.rgb(0, 0, 0, 0.35), 0);
        backdrop.setOnMousePressed(e -> close());

        cardHolder.setAlignment(Pos.CENTER);
        cardHolder.getChildren().add(loginCard());

        overlay.getChildren().addAll(backdrop, cardHolder);
    }

    void open() { shell.showOverlay(overlay); }

    private void close() { shell.hideOverlay(overlay); }

    private void setCard(Node card) { cardHolder.getChildren().setAll(card); }

    private VBox loginCard() {
        Label heading = Styles.label("Logg inn", 24, FontWeight.BOLD, Styles.NAME_DARK);
        TextField user = Styles.input(new TextField());
        user.setPromptText("Brukernavn");
        user.setPrefWidth(300);
        PasswordField pass = Styles.input(new PasswordField());
        pass.setPromptText("Passord");
        pass.setPrefWidth(300);
        Button login = Styles.primary("Logg inn");
        login.setPrefWidth(300);
        Button toRegister = Styles.primary("Registrer ny bruker");
        toRegister.setPrefWidth(300);
        Label error = Styles.error("");

        login.setOnAction(e -> {
            if (user.getText().isEmpty() || pass.getText().isEmpty()) {
                error.setText("Fyll ut begge feltene.");
                return;
            }
            error.setText("Logger inn…");
            login.setDisable(true);
            session.server().login(new LoginRequest(user.getText(), pass.getText()))
                .onFailure(err -> { login.setDisable(false); error.setText("Innlogging feilet: " + err.getMessage()); })
                .onSuccess(auth -> {
                    login.setDisable(false);
                    if (auth == null || auth.getToken() == null) {
                        error.setText("Innlogging feilet: feil brukernavn eller passord");
                        return;
                    }
                    session.setAuth(auth);
                    close();
                    onSuccess.run();
                });
        });
        toRegister.setOnAction(e -> setCard(registerCard()));

        return card(heading, user, pass, login, toRegister, error);
    }

    private VBox registerCard() {
        Label heading = Styles.label("Registrer ny bruker", 22, FontWeight.BOLD, Styles.NAME_DARK);
        TextField user = Styles.input(new TextField());
        user.setPromptText("Brukernavn");
        user.setPrefWidth(300);
        PasswordField pass1 = Styles.input(new PasswordField());
        pass1.setPromptText("Passord");
        pass1.setPrefWidth(300);
        PasswordField pass2 = Styles.input(new PasswordField());
        pass2.setPromptText("Gjenta passord");
        pass2.setPrefWidth(300);
        Button register = Styles.primary("Registrer");
        register.setPrefWidth(300);
        Button back = Styles.secondary("Tilbake til innlogging");
        back.setPrefWidth(300);
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
            session.server().register(new User(Ids.newId(), user.getText(), pass1.getText()))
                .onFailure(err -> { register.setDisable(false); error.setText("Registrering feilet: " + err.getMessage()); })
                .onSuccess(v -> { register.setDisable(false); setCard(loginCard()); });
        });
        back.setOnAction(e -> setCard(loginCard()));

        return card(heading, user, pass1, pass2, register, back, error);
    }

    private VBox card(Node... children) {
        VBox card = new VBox(12, children);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(360);
        Styles.whiteCard(card, 24, 12);
        return card;
    }
}
