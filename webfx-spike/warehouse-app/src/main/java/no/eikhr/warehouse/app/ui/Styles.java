package no.eikhr.warehouse.app.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Central brand styling for the warehouse app.
 *
 * <p><b>WebFX styling note (the reusable bit):</b> webfx-kit has no JavaFX CSS
 * engine — {@code Scene.getStylesheets()} and style classes are inert (there is
 * no {@code StyleManager}/{@code CssParser} in the kit). What IS honored is a
 * node's <em>inline</em> style: {@code node.setStyle("-fx-...")} is parsed by
 * {@code HtmlNodePeer.updateStyle} and each {@code -fx-foo} becomes a
 * {@code --fx-foo} CSS custom property on the element, which the kit's bundled
 * main.css maps to the real CSS property. So we style everything programmatically
 * via inline {@code -fx-} styles (colors, background, border, radius) plus JavaFX
 * node property APIs ({@code setFont}, {@code setPadding}, {@code setAlignment},
 * {@code setSpacing}). Padding uses {@code setPadding(Insets)} rather than the
 * 4-value {@code -fx-padding} shorthand (which the var-mapping can't split).
 */
final class Styles {
    static final String PURPLE      = "#840b9b";
    static final String DELETE_RED  = "#db1818";
    static final String PANEL       = "#f1f1f1";
    static final String ROW_LIGHT   = "#efefef";
    static final String ROW_DARK    = "#e4e4e4";
    static final String SCROLL_BG   = "#f9f9f9";
    static final String TEXT_DARK   = "#333333";
    static final String BORDER_GREY = "#cccccc";

    private Styles() {}

    static Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 20));
        l.setStyle("-fx-text-fill: white;");
        return l;
    }

    static Label heading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 22));
        l.setStyle("-fx-text-fill: " + PURPLE + ";");
        return l;
    }

    static Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        l.setStyle("-fx-text-fill: " + TEXT_DARK + ";");
        return l;
    }

    static Label error(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + DELETE_RED + ";");
        return l;
    }

    static Button primary(String text) {
        Button b = base(text);
        b.setStyle("-fx-background-color: " + PURPLE + "; -fx-text-fill: white;"
            + " -fx-background-radius: 8px; -fx-border-radius: 8px;");
        return b;
    }

    static Button danger(String text) {
        Button b = base(text);
        b.setStyle("-fx-background-color: " + DELETE_RED + "; -fx-text-fill: white;"
            + " -fx-background-radius: 8px; -fx-border-radius: 8px;");
        return b;
    }

    static Button secondary(String text) {
        Button b = base(text);
        b.setStyle("-fx-background-color: rgba(0,0,0,0.06); -fx-text-fill: " + TEXT_DARK + ";"
            + " -fx-background-radius: 8px; -fx-border-radius: 8px;");
        return b;
    }

    /** White button with purple text — the title-bar login/logout button. */
    static Button ghost(String text) {
        Button b = base(text);
        b.setPadding(new Insets(6, 14, 6, 14));
        b.setStyle("-fx-background-color: white; -fx-text-fill: " + PURPLE + ";"
            + " -fx-background-radius: 8px; -fx-border-radius: 8px;");
        return b;
    }

    private static Button base(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("System", FontWeight.BOLD, 13));
        b.setPadding(new Insets(8, 18, 8, 18));
        b.setCursor(Cursor.HAND);
        return b;
    }

    static <T extends TextField> T input(T tf) {
        tf.setPadding(new Insets(7, 10, 7, 10));
        tf.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER_GREY + ";"
            + " -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px;"
            + " -fx-focus-color: " + PURPLE + ";");
        return tf;
    }

    /** Light rounded panel background (the inputGroup look). */
    static void panel(Region r) {
        r.setPadding(new Insets(16));
        r.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 10px;");
    }
}
