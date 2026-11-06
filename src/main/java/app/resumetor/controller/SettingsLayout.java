package app.resumetor.controller;

import app.resumetor.importexport.VaultManager;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class SettingsLayout extends ScrollPane {

    private final VBox container;

    public SettingsLayout() {
        container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setMaxWidth(600);
        
        this.setContent(container);
        this.setFitToWidth(true);
        this.getStyleClass().add("main-content");
        
        buildUi();
    }

    private void buildUi() {
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        VBox dataSection = new VBox(10);
        dataSection.setPadding(new Insets(20));
        dataSection.setStyle("-fx-background-color: -panel-bg; -fx-background-radius: 8px; -fx-border-color: -border-color; -fx-border-radius: 8px;");
        
        Label dataTitle = new Label("Data & Portability");
        dataTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label dataDesc = new Label("Export your Career Profile and applications to a .resumevault file, or import from one.");
        dataDesc.setStyle("-fx-text-fill: -text-secondary;");
        
        Button exportBtn = new Button("Export Data");
        exportBtn.getStyleClass().addAll("btn", "btn-primary");
        exportBtn.setGraphic(new FontIcon("fth-download"));
        exportBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Resume Vault", "*.resumevault"));
            fileChooser.setInitialFileName("my-career.resumevault");
            File file = fileChooser.showSaveDialog(container.getScene().getWindow());
            if (file != null) {
                try {
                    new VaultManager().exportVault(file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        dataSection.getChildren().addAll(dataTitle, dataDesc, exportBtn);
        container.getChildren().addAll(title, dataSection);
    }
}
