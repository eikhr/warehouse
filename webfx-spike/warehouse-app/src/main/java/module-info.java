// File managed by WebFX (DO NOT EDIT MANUALLY)

module warehouse.app {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.platform.ast;
    requires webfx.platform.ast.json.plugin;
    requires webfx.platform.async;
    requires webfx.platform.fetch;
    requires webfx.platform.fetch.ast.json;
    requires webfx.platform.resource;

    // Exported packages
    exports no.eikhr.warehouse.app;
    exports no.eikhr.warehouse.app.client;
    exports no.eikhr.warehouse.app.json;
    exports no.eikhr.warehouse.app.model;
    exports no.eikhr.warehouse.app.session;
    exports no.eikhr.warehouse.app.ui;

    // Resources packages
    opens no.eikhr.warehouse.app.icons;

    // Provided services
    provides javafx.application.Application with no.eikhr.warehouse.app.WarehouseApp;

}