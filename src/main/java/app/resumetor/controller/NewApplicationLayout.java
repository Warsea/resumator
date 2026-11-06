package app.resumetor.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class NewApplicationLayout extends ScrollPane {

    private final VBox container;

    public NewApplicationLayout() {
        container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setMaxWidth(900);
        
        this.setContent(container);
        this.setFitToWidth(true);
        this.getStyleClass().add("main-content");
        
        buildUi();
    }

    private void buildUi() {
        Label title = new Label("New Job Application");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Paste the job description and requirements here.");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: -text-secondary;");
        
        VBox header = new VBox(5, title, subtitle);
        header.setPadding(new Insets(0, 0, 15, 0));

        TextArea jobDescriptionArea = new TextArea();
        jobDescriptionArea.setPromptText("Paste complete job description, requirements, and responsibilities here...");
        jobDescriptionArea.setPrefRowCount(15);
        jobDescriptionArea.setWrapText(true);
        jobDescriptionArea.setStyle("-fx-font-family: monospace; -fx-padding: 10px;");

        // Optional Metadata
        Label metaLabel = new Label("Optional Metadata");
        metaLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(15);
        metaGrid.setVgap(15);
        
        TextField companyField = createField(metaGrid, "Company", 0, 0);
        TextField titleField = createField(metaGrid, "Job Title", 1, 0);
        TextField locationField = createField(metaGrid, "Location", 0, 1);
        TextField deadlineField = createField(metaGrid, "Deadline", 1, 1);
        TextField urlField = createField(metaGrid, "Job URL", 0, 2);
        GridPane.setColumnSpan(urlField, 3);
        urlField.setPrefWidth(600);

        VBox metaSection = new VBox(10, metaLabel, metaGrid);
        metaSection.setPadding(new Insets(15));
        metaSection.setStyle("-fx-background-color: -panel-bg; -fx-background-radius: 8px; -fx-border-color: -border-color; -fx-border-radius: 8px;");

        // Submit Button
        Button analyzeBtn = new Button("Analyze & Generate Application");
        analyzeBtn.getStyleClass().addAll("btn", "btn-primary");
        analyzeBtn.setGraphic(new FontIcon("fth-cpu"));
        analyzeBtn.setStyle("-fx-font-size: 16px; -fx-padding: 12px 24px;");
        
        analyzeBtn.setOnAction(e -> {
            String rawJobText = jobDescriptionArea.getText();
            if (rawJobText == null || rawJobText.trim().isEmpty()) {
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
                        "Please paste a job description first.").showAndWait();
                return;
            }
            MainLayout main = getScene() != null && getScene().getRoot() instanceof MainLayout m ? m : null;

            analyzeBtn.setDisable(true);
            analyzeBtn.setText("Analyzing...");

            // Persist the application right away so it shows in the sidebar.
            app.resumetor.repository.ApplicationDao appDao = new app.resumetor.repository.ApplicationDao();
            app.resumetor.model.JobApplication application = new app.resumetor.model.JobApplication();
            application.setJobTitle(titleField.getText().trim());
            application.setCompany(companyField.getText().trim());
            application.setLocation(locationField.getText().trim());
            application.setDeadline(deadlineField.getText().trim());
            application.setJobUrl(urlField.getText().trim());
            application.setRawJobText(rawJobText);
            int appId = appDao.insertApplication(application);
            if (main != null) main.refreshApplications();

            app.resumetor.service.AiService aiService = new app.resumetor.service.AiService();
            java.util.concurrent.CompletableFuture<String> jobAnalysisFuture = aiService.analyzeJob(rawJobText);
            java.util.concurrent.CompletableFuture<String> matchingAnalysisFuture = jobAnalysisFuture.thenCompose(aiService::matchProfileToJob);
            java.util.concurrent.CompletableFuture<String> resumeFuture = jobAnalysisFuture.thenCombine(matchingAnalysisFuture, (job, match) -> new String[]{job, match})
                    .thenCompose(arr -> aiService.generateResume(arr[0], arr[1]));
            java.util.concurrent.CompletableFuture<String> coverLetterFuture = jobAnalysisFuture.thenCombine(matchingAnalysisFuture, (job, match) -> new String[]{job, match})
                    .thenCompose(arr -> aiService.generateCoverLetter(arr[0], arr[1]));
            java.util.concurrent.CompletableFuture<String> emailFuture = jobAnalysisFuture.thenCompose(aiService::generateEmail);

            if (appId > 0) {
                jobAnalysisFuture.thenAccept(json -> {
                    appDao.saveDocument(appId, app.resumetor.repository.ApplicationDao.JOB_ANALYSIS, json);
                    com.fasterxml.jackson.databind.JsonNode node = app.resumetor.util.JsonText.parse(json);
                    appDao.updateTitleCompany(appId,
                            app.resumetor.util.JsonText.text(node, "jobTitle"),
                            app.resumetor.util.JsonText.text(node, "company"));
                    if (main != null) javafx.application.Platform.runLater(main::refreshApplications);
                });
                matchingAnalysisFuture.thenAccept(c -> appDao.saveDocument(appId, app.resumetor.repository.ApplicationDao.MATCHING, c));
                resumeFuture.thenAccept(c -> appDao.saveDocument(appId, app.resumetor.repository.ApplicationDao.RESUME, c));
                coverLetterFuture.thenAccept(c -> appDao.saveDocument(appId, app.resumetor.repository.ApplicationDao.COVER_LETTER, c));
                emailFuture.thenAccept(c -> appDao.saveDocument(appId, app.resumetor.repository.ApplicationDao.EMAIL, c));
            }

            GeneratedApplicationLayout generatedView = new GeneratedApplicationLayout(
                    application.getDisplayName(), matchingAnalysisFuture, resumeFuture, coverLetterFuture, emailFuture);
            if (main != null) main.setMainContent(generatedView);
        });
        
        HBox actions = new HBox(analyzeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 0, 0, 0));

        container.getChildren().addAll(header, jobDescriptionArea, metaSection, actions);
    }
    
    private TextField createField(GridPane grid, String labelText, int col, int row) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: -text-secondary;");
        TextField field = new TextField();
        field.setPrefWidth(300);
        box.getChildren().addAll(label, field);
        
        grid.add(box, col, row);
        return field;
    }
}
