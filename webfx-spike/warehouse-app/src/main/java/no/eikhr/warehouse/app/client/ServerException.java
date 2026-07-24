package no.eikhr.warehouse.app.client;

/** Carries an HTTP status + message (replaces {@code ui.ServerError}). */
public class ServerException extends RuntimeException {
    private final int status;

    public ServerException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
