package no.eikhr.warehouse.app;

import no.eikhr.warehouse.app.session.Session;
import no.eikhr.warehouse.app.ui.AppShell;
import no.eikhr.warehouse.app.ui.ServerSelectView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point. Wires the {@link AppShell} and shows the first view
 * (ServerSelect → Login/Register → item list → item detail).
 *
 * <p>Networking (formerly {@code java.net.http} + Jackson) is replaced by WebFX
 * Fetch + AST-JSON so the whole client transpiles to JS.
 */
public class WarehouseApp extends Application {

    @Override
    public void start(Stage stage) {
        Session session = new Session();
        AppShell shell = new AppShell();
        shell.show(new ServerSelectView(session, shell));
        stage.setScene(new Scene(shell.getRoot(), 900, 640));
        stage.setTitle("Warehouse");
        stage.show();
    }
}
