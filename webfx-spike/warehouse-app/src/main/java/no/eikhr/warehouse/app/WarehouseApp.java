package no.eikhr.warehouse.app;

import no.eikhr.warehouse.app.session.Session;
import no.eikhr.warehouse.app.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point. Wires the {@link AppShell} (a StackPane root so login modals can
 * overlay) and shows ServerSelect → read-only item list. Login is optional and
 * only unlocks editing.
 *
 * <p>Networking (formerly {@code java.net.http} + Jackson) is replaced by WebFX
 * Fetch + AST-JSON so the whole client transpiles to JS.
 */
public class WarehouseApp extends Application {

    @Override
    public void start(Stage stage) {
        Session session = new Session();
        AppShell shell = new AppShell(session);
        shell.showServerSelect();
        stage.setScene(new Scene(shell.getRoot(), 1000, 680));
        stage.setTitle("Warehouse");
        stage.show();
    }
}
