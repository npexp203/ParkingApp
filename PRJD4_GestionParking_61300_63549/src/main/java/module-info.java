module be.esi.prj {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    requires tess4j;
    requires opencv;
    requires javafx.graphics;
    requires java.desktop;


    exports be.esi.prj;
    exports be.esi.prj.model;
    opens be.esi.prj.view to javafx.fxml;
}
