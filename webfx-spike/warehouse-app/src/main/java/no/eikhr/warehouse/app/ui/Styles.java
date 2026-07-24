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
 * <p><b>WebFX styling note (the reusable bit):</b> webfx-kit has <em>no</em>
 * JavaFX CSS engine. There is no {@code CssParser}/{@code StyleManager}, so a
 * node's {@code setStyle("-fx-...")} string is <b>inert</b> — nothing parses it
 * into node properties (verified in the live DOM: no {@code --fx-*} vars appear,
 * every background computes to white). {@code Scene.getStylesheets()} and style
 * classes are inert too. What renders is the scene-graph <b>property API</b>: the
 * HTML peers map {@code setBackground}, {@code setBorder}, {@code setTextFill},
 * {@code setFont}, {@code setPadding} and {@code setAlignment} onto the DOM
 * element. So every visual here is applied through those APIs — never a CSS
 * string. Buttons/TextFields/panes all extend {@link Region}, so
 * {@code setBackground}/{@code setBorder} work on them.
 */
final class Styles {
    static final Color PURPLE        = Color.web("#840b9b");
    static final Color DELETE_RED    = Color.web("#db1818");
    static final Color PANEL         = Color.web("#f1f1f1");
    static final Color ROW_LIGHT     = Color.web("#efefef");
    static final Color ROW_DARK      = Color.web("#e4e4e4");
    static final Color SCROLL_BG     = Color.web("#f9f9f9");
    static final Color TEXT_DARK     = Color.web("#333333");
    static final Color BORDER_GREY   = Color.web("#cccccc");
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

    static Label title(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 20));
        l.setTextFill(Color.WHITE);
        return l;
    }

    static Label heading(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 22));
        l.setTextFill(PURPLE);
        return l;
    }

    static Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        l.setTextFill(TEXT_DARK);
        return l;
    }

    static Label error(String text) {
        Label l = new Label(text);
        l.setTextFill(DELETE_RED);
        return l;
    }

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

    /** Light rounded panel background (the inputGroup look). */
    static void panel(Region r) {
        r.setPadding(new Insets(16));
        bg(r, PANEL, 10);
    }
}
