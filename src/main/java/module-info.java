module com.aircraft {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.web;
    requires kernel;
    requires layout;
    requires io;

    // Open all packages to JavaFX for reflection
    opens com.aircraft to javafx.fxml;
    opens com.aircraft.controller to javafx.fxml;
    opens com.aircraft.model to javafx.base;
    opens com.aircraft.dao to javafx.base;


    // Export packages
    exports com.aircraft;
    exports com.aircraft.controller;
    exports com.aircraft.model;




}