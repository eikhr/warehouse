// File managed by WebFX (DO NOT EDIT MANUALLY)

module warehouse.app {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.platform.ast;
    requires webfx.platform.fetch.ast.json;

    // Exported packages
    exports no.eikhr.warehouse.app;

    // Provided services
    provides javafx.application.Application with no.eikhr.warehouse.app.WarehouseApp;

}