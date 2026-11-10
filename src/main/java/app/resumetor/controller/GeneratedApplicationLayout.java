package app.resumetor.controller;

import app.resumetor.export.DocumentFormatter;
import app.resumetor.export.DocxExporter;
import app.resumetor.export.PdfExporter;
import app.resumetor.model.Profile;
import app.resumetor.model.ProfileLink;
import app.resumetor.repository.CareerDao;
import app.resumetor.repository.ProfileDao;
import app.resumetor.security.PiiSanitizer;
import app.resumetor.util.JsonText;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class GeneratedApplicationLayout extends BorderPane {

    private final TabPane tabPane;
    private final Profile profile;
    private final List<ProfileLink> links;
    private final CareerDao careerDao = new CareerDao();

    public GeneratedApplicationLayout(
            String headerTitle,
            CompletableFuture<String> matchingAnalysis,
            CompletableFuture<String> resumeContent,
            CompletableFuture<String> coverLetterContent,
            CompletableFuture<String> emailContent) {

        ProfileDao profileDao = new ProfileDao();
        this.profile = profileDao.getProfile();
        this.links = profileDao.getLinks();

        this.getStyleClass().add("main-content");

        VBox header = new VBox(5);
        header.setPadding(new Insets(20, 30, 0, 30));
        Label title = new Label(headerTitle == null || headerTitle.isBlank() ? "Application Workspace" : headerTitle);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        header.getChildren().add(title);
        this.setTop(header);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-padding: 10px 30px 30px 30px;");

        String datePart = LocalDate.now().toString();
        String namePart = safeFileName(profile != null ? profile.getFullName() : "Resume");

        Tab resumeTab = createLoadingTab("Resume", resumeContent, raw -> buildDocumentView(
                DocumentFormatter.resumeToMarkup(raw, profile, links, careerDao.getAllEducations()),
                namePart + "-CV-" + datePart));
        Tab coverTab = createLoadingTab("Cover Letter", coverLetterContent, raw -> buildDocumentView(
                DocumentFormatter.coverLetterToMarkup(PiiSanitizer.injectRealPii(raw, profile), profile, links),
                namePart + "-CoverLetter-" + datePart));
        Tab emailTab = createLoadingTab("Email", emailContent, raw -> buildEmailView(
                PiiSanitizer.injectRealPii(DocumentFormatter.plainText(raw, "email", "body", "content"), profile)));
        Tab fitTab = createLoadingTab("Job Fit & Feedback", matchingAnalysis, this::buildFeedbackView);

        tabPane.getTabs().addAll(resumeTab, coverTab, emailTab, fitTab);
        this.setCenter(tabPane);
    }

    private Tab createLoadingTab(String title, CompletableFuture<String> future, Function<String, Node> viewFactory) {
        Tab tab = new Tab(title);

        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.getChildren().addAll(new ProgressIndicator(), new Label("Generating " + title + "... Do not leave this page."));
        tab.setContent(loadingBox);

        future.thenAccept(content -> Platform.runLater(() -> {
            if (content == null || content.isBlank()) {
                tab.setContent(errorLabel("No " + title.toLowerCase() + " content available."));
                return;
            }
            try {
                tab.setContent(viewFactory.apply(content));
            } catch (Exception ex) {
                ex.printStackTrace();
                tab.setContent(errorLabel("Failed to display " + title + ": " + ex.getMessage()));
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> tab.setContent(errorLabel("Failed to generate " + title + ": " + ex.getMessage())));
            return null;
        });
        return tab;
    }

    // ---------------------------------------------------------------- Resume / Cover letter

    private Node buildDocumentView(String markup, String baseFileName) {
        VBox pages = new VBox(20);
        pages.setAlignment(Pos.TOP_CENTER);
        pages.setPadding(new Insets(20));
        pages.setStyle("-fx-background-color: #2a2d34;");

        ScrollPane previewScroll = new ScrollPane(pages);
        previewScroll.setFitToWidth(true);
        VBox.setVgrow(previewScroll, Priority.ALWAYS);

        TextArea editor = new TextArea(markup);
        editor.setWrapText(true);
        editor.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        VBox.setVgrow(editor, Priority.ALWAYS);

        StackPane body = new StackPane(previewScroll);
        VBox.setVgrow(body, Priority.ALWAYS);

        renderPreview(editor.getText(), pages);

        ToggleButton editToggle = new ToggleButton("Edit Text");
        editToggle.setGraphic(new FontIcon("fth-edit-2"));
        editToggle.getStyleClass().add("btn");
        editToggle.setOnAction(e -> {
            if (editToggle.isSelected()) {
                editToggle.setText("Show Preview");
                editToggle.setGraphic(new FontIcon("fth-eye"));
                body.getChildren().setAll(editor);
            } else {
                editToggle.setText("Edit Text");
                editToggle.setGraphic(new FontIcon("fth-edit-2"));
                renderPreview(editor.getText(), pages);
                body.getChildren().setAll(previewScroll);
            }
        });

        Button copyBtn = toolButton("Copy Text", "fth-copy");
        copyBtn.setOnAction(e -> copyToClipboard(editor.getText()));

        Button pdfBtn = toolButton("Export PDF", "fth-download");
        pdfBtn.setOnAction(e -> exportFile(editor.getText(), baseFileName + ".pdf", "PDF Document", "*.pdf", true));

        Button docxBtn = toolButton("Export DOCX", "fth-file-text");
        docxBtn.setOnAction(e -> exportFile(editor.getText(), baseFileName + ".docx", "Word Document", "*.docx", false));

        HBox toolBar = new HBox(10, editToggle, spacer(), copyBtn, docxBtn, pdfBtn);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(10, toolBar, body);
        content.setPadding(new Insets(15));
        return content;
    }

    private void renderPreview(String markup, VBox pagesBox) {
        pagesBox.getChildren().setAll(new ProgressIndicator());
        CompletableFuture.supplyAsync(() -> {
            List<WritableImage> images = new ArrayList<>();
            try (PDDocument doc = new PdfExporter().build(markup)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                for (int i = 0; i < doc.getNumberOfPages(); i++) {
                    images.add(toFxImage(renderer.renderImageWithDPI(i, 110)));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return images;
        }).thenAccept(images -> Platform.runLater(() -> {
            pagesBox.getChildren().clear();
            for (WritableImage img : images) {
                ImageView view = new ImageView(img);
                view.setPreserveRatio(true);
                view.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 16, 0, 0, 4);");
                pagesBox.getChildren().add(view);
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> pagesBox.getChildren().setAll(errorLabel("Preview failed: " + ex.getMessage())));
            return null;
        });
    }

    private static WritableImage toFxImage(BufferedImage bi) {
        WritableImage img = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                pw.setArgb(x, y, bi.getRGB(x, y) | 0xFF000000);
            }
        }
        return img;
    }

    private void exportFile(String markup, String fileName, String desc, String ext, boolean pdf) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        chooser.setInitialFileName(fileName);
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;
        try {
            if (pdf) new PdfExporter().export(markup, file);
            else new DocxExporter().export(markup, file);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
        }
    }

    // ---------------------------------------------------------------- Email

    private Node buildEmailView(String email) {
        TextArea area = new TextArea(email);
        area.setWrapText(true);
        area.setStyle("-fx-font-size: 14px;");
        VBox.setVgrow(area, Priority.ALWAYS);

        Button copyBtn = toolButton("Copy Email", "fth-copy");
        copyBtn.setOnAction(e -> copyToClipboard(area.getText()));

        HBox toolBar = new HBox(10, spacer(), copyBtn);
        VBox box = new VBox(10, toolBar, area);
        box.setPadding(new Insets(15));
        return box;
    }

    // ---------------------------------------------------------------- Feedback

    private Node buildFeedbackView(String raw) {
        VBox box = new VBox(18);
        box.setPadding(new Insets(20));

        JsonNode root = JsonText.parse(raw);
        if (root == null || !root.isObject()) {
            box.getChildren().add(paragraph(raw));
        } else {
            Iterator<Map.Entry<String, JsonNode>> it = root.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                String text = nodeToProse(entry.getValue());
                if (text.isBlank()) continue;

                Label heading = new Label(JsonText.humanize(entry.getKey()));
                heading.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
                VBox section = new VBox(6, heading, paragraph(text));
                section.setPadding(new Insets(14));
                section.setStyle("-fx-background-color: -panel-bg; -fx-background-radius: 8px; -fx-border-color: -border-color; -fx-border-radius: 8px;");
                box.getChildren().add(section);
            }
        }

        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static String nodeToProse(JsonNode node) {
        if (node == null || node.isNull()) return "";
        if (node.isArray()) {
            List<String> parts = new ArrayList<>();
            node.forEach(n -> {
                String s = nodeToProse(n);
                if (!s.isBlank()) parts.add("• " + s);
            });
            return String.join("\n", parts);
        }
        if (node.isObject()) {
            List<String> parts = new ArrayList<>();
            node.fields().forEachRemaining(e -> parts.add(JsonText.humanize(e.getKey()) + ": " + nodeToProse(e.getValue())));
            return String.join(" — ", parts);
        }
        return node.asText().trim();
    }

    // ---------------------------------------------------------------- helpers

    private static Label paragraph(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 14px; -fx-line-spacing: 3px;");
        return l;
    }

    private static Label errorLabel(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: -danger; -fx-font-weight: bold;");
        return l;
    }

    private static Button toolButton(String text, String icon) {
        Button b = new Button(text);
        b.setGraphic(new FontIcon(icon));
        b.getStyleClass().add("btn");
        return b;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private static void copyToClipboard(String text) {
        javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
        c.putString(text);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(c);
    }

    private static String safeFileName(String name) {
        if (name == null || name.isBlank()) return "Resume";
        return name.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "");
    }
}
