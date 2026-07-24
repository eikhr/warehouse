package no.eikhr.warehouse.app.ui;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Central brand styling for the warehouse app.
 *
 * <p><b>WebFX styling note:</b> webfx-kit has <em>no</em> JavaFX CSS engine, so a
 * node's {@code setStyle("-fx-...")} string is <b>inert</b>. Everything here is
 * applied through the scene-graph <b>property API</b> — {@code setBackground},
 * {@code setBorder}, {@code setTextFill}, {@code setFont}, {@code setPadding},
 * {@code setAlignment}, {@code setSpacing} — which the HTML peers map to the DOM.
 * Buttons/TextFields/panes all extend {@link Region}, so
 * {@code setBackground}/{@code setBorder} work on them.
 */
final class Styles {
    static final Color PURPLE        = Color.web("#840b9b");
    static final Color PURPLE_DARK   = Color.web("#5f0a70");
    static final Color DELETE_RED    = Color.web("#db1818");
    static final Color PANEL         = Color.web("#f1f1f1");
    static final Color ROW_A         = Color.web("#f4f4f4");
    static final Color ROW_B         = Color.WHITE;
    static final Color HEADER_GREY   = Color.web("#dcdcdc");
    static final Color SCROLL_BG     = Color.web("#f9f9f9");
    static final Color TEXT_DARK     = Color.web("#333333");
    static final Color NAME_DARK     = Color.web("#222222");
    static final Color GREY_TEXT     = Color.web("#555555");
    static final Color CAPTION_GREY  = Color.web("#777777");
    static final Color BORDER_GREY   = Color.web("#cccccc");
    static final Color STEP_GREY     = Color.web("#e0e0e0");
    static final Color BTN_SECONDARY = Color.rgb(0, 0, 0, 0.06);

    private Styles() {}

    /** Solid rounded background via the property API (setStyle is inert in WebFX). */
    static void bg(Region r, Color c, double radius) {
        r.setBackground(new Background(new BackgroundFill(c, new CornerRadii(radius), Insets.EMPTY)));
    }

    /** Solid rounded border via the property API. */
    static void border(Region r, Color c, double w, double radius) {
        r.setBorder(new Border(new BorderStroke(c, BorderStrokeStyle.SOLID,
            new CornerRadii(radius), new BorderWidths(w))));
    }

    static Label label(String text, double size, FontWeight weight, Color fill) {
        Label l = new Label(text);
        l.setFont(Font.font("System", weight, size));
        l.setTextFill(fill);
        return l;
    }

    static Label title(String text) { return label(text, 24, FontWeight.BOLD, Color.WHITE); }
    static Label heading(String text) { return label(text, 22, FontWeight.BOLD, PURPLE); }
    static Label sectionHeading(String text) { return label(text, 15, FontWeight.BOLD, NAME_DARK); }
    static Label fieldLabel(String text) { return label(text, 13, FontWeight.BOLD, TEXT_DARK); }
    static Label caption(String text) { return label(text, 11, FontWeight.NORMAL, CAPTION_GREY); }
    static Label error(String text) { return label(text, 13, FontWeight.NORMAL, DELETE_RED); }

    static Button primary(String text) {
        Button b = base(text);
        bg(b, PURPLE, 8);
        b.setTextFill(Color.WHITE);
        return b;
    }

    static Button danger(String text) {
        Button b = base(text);
        bg(b, DELETE_RED, 8);
        b.setTextFill(Color.WHITE);
        return b;
    }

    static Button secondary(String text) {
        Button b = base(text);
        bg(b, BTN_SECONDARY, 8);
        b.setTextFill(TEXT_DARK);
        return b;
    }

    /** White button with purple text — the title-bar login/logout button. */
    static Button ghost(String text) {
        Button b = base(text);
        b.setPadding(new Insets(6, 14, 6, 14));
        bg(b, Color.WHITE, 8);
        b.setTextFill(PURPLE);
        return b;
    }

    /** White, bordered, rounded button used as a menu-button ("Sorter ▾") — no ComboBox in WebFX. */
    static Button menuButton(String text) {
        Button b = base(text);
        b.setFont(Font.font("System", FontWeight.NORMAL, 13));
        bg(b, Color.WHITE, 8);
        border(b, BORDER_GREY, 1, 8);
        b.setTextFill(TEXT_DARK);
        return b;
    }

    /** Round grey button for the −/+ stepper. */
    static Button round(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("System", FontWeight.BOLD, 18));
        b.setMinSize(36, 36);
        b.setPrefSize(36, 36);
        b.setMaxSize(36, 36);
        b.setCursor(Cursor.HAND);
        bg(b, STEP_GREY, 18);
        b.setTextFill(NAME_DARK);
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
        bg(tf, Color.WHITE, 6);
        border(tf, BORDER_GREY, 2, 6);
        return tf;
    }

    /** Light rounded grey panel (section background / inputGroup look). */
    static void panel(Region r) {
        r.setPadding(new Insets(16));
        bg(r, PANEL, 10);
    }

    /** White rounded bordered card (modal / location boxes). */
    static void whiteCard(Region r, double pad, double radius) {
        r.setPadding(new Insets(pad));
        bg(r, Color.WHITE, radius);
        border(r, BORDER_GREY, 1, radius);
    }
}
