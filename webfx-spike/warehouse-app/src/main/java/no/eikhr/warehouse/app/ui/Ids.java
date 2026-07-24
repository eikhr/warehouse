package no.eikhr.warehouse.app.ui;

/** Client-side id generation (no {@code java.util.UUID} under transpilation). */
final class Ids {
    private Ids() {}

    static String newId() {
        return "web-" + Long.toHexString(System.currentTimeMillis())
            + "-" + Integer.toHexString((int) (Math.random() * 0x7fffffff));
    }
}
