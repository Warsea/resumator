module app.resumetor {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;
    requires org.apache.poi.ooxml;
    requires org.apache.pdfbox;
    requires io.github.cdimascio.dotenv.java;
    requires org.slf4j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    
    exports app.resumetor;
}
