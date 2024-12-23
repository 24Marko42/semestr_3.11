module org.example.dem {
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires spring.security.crypto;
    requires spring.security.core;
    requires org.json;
    requires java.rmi;
    requires jdk.jdeps;
    requires java.net.http;

    opens org.example.dem to javafx.fxml, javafx.graphics;
    exports org.example.dem;
}
