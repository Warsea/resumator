package app.resumetor.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class MainLayout extends BorderPane {

    private final VBox sidebar;
    private final StackPane mainContent;
    private final StackPane pageHolder;
    
    // UI Elements
    private VBox applicationsList;

    public MainLayout() {
        pageHolder = new StackPane();
        sidebar = createSidebar();
        mainContent = createMainContent();
        
        setLeft(sidebar);
        setCenter(mainContent);
        
        getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        getStyleClass().add("app-background");
    }

    private VBox createSidebar() {
        VBox sidebarBox = new VBox(15);
        sidebarBox.setPadding(new Insets(20));
        sidebarBox.setPrefWidth(260);
        sidebarBox.getStyleClass().add("sidebar");

        // Logo
        // Image logoImg = new Image(getClass().getResourceAsStream("/images/icon.png"));
        // ImageView logoView = new ImageView(logoImg);
        // logoView.setFitWidth(24);
        // logoView.setFitHeight(24);
        // logoView.setPreserveRatio(true);

        // Header / Logo
        Label appTitle = new Label("Resumetor");
        appTitle.getStyleClass().add("sidebar-title");
        appTitle.setGraphic(new FontIcon("fth-briefcase")); // uncomment the above logo section and put the logoView in setGraphic if you want to use an image instead of an icon
        appTitle.setGraphicTextGap(10);
        
        // New Application Button
        Button newAppBtn = new Button("New Application");
        newAppBtn.getStyleClass().addAll("btn", "btn-primary");
        newAppBtn.setMaxWidth(Double.MAX_VALUE);
        newAppBtn.setGraphic(new FontIcon("fth-plus"));
        newAppBtn.setOnAction(e -> setMainContent(new NewApplicationLayout()));
        
        // Applications List
        Label appsLabel = new Label("Applications");
        appsLabel.getStyleClass().add("sidebar-section-title");
        
        applicationsList = new VBox(6);
        ScrollPane appsScroll = new ScrollPane(applicationsList);
        appsScroll.setFitToWidth(true);
        appsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(appsScroll, Priority.ALWAYS);
        
        // Bottom Section (Profile, Settings)
        VBox bottomNav = new VBox(5);
        Button profileBtn = createNavButton("My Career Profile", "fth-user");
        profileBtn.setOnAction(e -> setMainContent(new ProfileDashboardLayout()));
        
        Button settingsBtn = createNavButton("Settings", "fth-settings");
        settingsBtn.setOnAction(e -> setMainContent(new SettingsLayout()));
        bottomNav.getChildren().addAll(profileBtn, settingsBtn);

        sidebarBox.getChildren().addAll(appTitle, newAppBtn, appsLabel, appsScroll, bottomNav);
        refreshApplications();
        return sidebarBox;
    }

    /** Reloads the saved applications from the database into the sidebar. */
    public void refreshApplications() {
        applicationsList.getChildren().clear();
        var apps = new app.resumetor.repository.ApplicationDao().getAll();
        if (apps.isEmpty()) {
            Label noApps = new Label("No applications yet");
            noApps.setStyle("-fx-text-fill: -text-secondary; -fx-padding: 0 0 0 10px;");
            applicationsList.getChildren().add(noApps);
            return;
        }
        for (var a : apps) {
            Button open = createNavButton(a.getDisplayName(), "fth-file-text");
            open.setMaxWidth(Double.MAX_VALUE);
            open.setTooltip(new javafx.scene.control.Tooltip(a.getDisplayName()
                    + (a.getCreatedAt() != null ? "\n" + a.getCreatedAt() : "")));
            open.setOnAction(e -> openApplication(a.getId()));
            HBox.setHgrow(open, Priority.ALWAYS);

            Button del = new Button();
            del.setGraphic(new FontIcon("fth-trash-2"));
            del.getStyleClass().add("btn-nav");
            del.setOnAction(e -> {
                var confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION,
                        "Delete \"" + a.getDisplayName() + "\"?");
                if (confirm.showAndWait().filter(b -> b == javafx.scene.control.ButtonType.OK).isPresent()) {
                    new app.resumetor.repository.ApplicationDao().delete(a.getId());
                    refreshApplications();
                }
            });
            HBox row = new HBox(2, open, del);
            row.setAlignment(Pos.CENTER_LEFT);
            applicationsList.getChildren().add(row);
        }
    }

    private void openApplication(int id) {
        var a = new app.resumetor.repository.ApplicationDao().getWithDocuments(id);
        if (a == null) return;
        setMainContent(new GeneratedApplicationLayout(a.getDisplayName(),
                java.util.concurrent.CompletableFuture.completedFuture(a.getMatchingAnalysis()),
                java.util.concurrent.CompletableFuture.completedFuture(a.getResume()),
                java.util.concurrent.CompletableFuture.completedFuture(a.getCoverLetter()),
                java.util.concurrent.CompletableFuture.completedFuture(a.getEmail())));
    }

    private StackPane createMainContent() {
        StackPane contentPane = new StackPane();
        contentPane.getStyleClass().add("main-content");
        contentPane.setPadding(new Insets(30));
        
        // Placeholder for content
        Label placeholder = new Label("Welcome to Resumetor. Select or create an application to get started.");
        placeholder.getStyleClass().add("placeholder-text");
        pageHolder.getChildren().add(placeholder);
        contentPane.getChildren().add(pageHolder);
        
        return contentPane;
    }
    
    public void setMainContent(javafx.scene.Node node) {
        pageHolder.getChildren().setAll(node);
    }

    public void showToast(String message) {
        HBox toast = new HBox(12);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(14, 18, 14, 18));
        toast.setMaxWidth(520);
        toast.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        toast.setStyle(
                "-fx-background-color: #1E1B4B;" +
                "-fx-background-radius: 10px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 16, 0, 0, 4);"
        );

        FontIcon icon = new FontIcon("fth-info");
        icon.setStyle("-fx-icon-color: #818CF8; -fx-icon-size: 20px;");

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 13px;");
        HBox.setHgrow(msgLabel, Priority.ALWAYS);

        Button profileBtn = new Button("Go to Profile");
        profileBtn.setStyle(
                "-fx-background-color: #4F46E5; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6px 12px; -fx-background-radius: 6px; -fx-cursor: hand;"
        );
        profileBtn.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        profileBtn.setOnAction(e -> {
            mainContent.getChildren().remove(toast);
            setMainContent(new ProfileDashboardLayout());
        });

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon("fth-x");
        closeIcon.setStyle("-fx-icon-color: #A5B4FC; -fx-icon-size: 16px;");
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-padding: 4px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> mainContent.getChildren().remove(toast));

        toast.getChildren().addAll(icon, msgLabel, profileBtn, closeBtn);
        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toast, new Insets(0, 10, 10, 0));

        mainContent.getChildren().add(toast);

        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(10));
        delay.setOnFinished(e -> {
            if (mainContent.getChildren().contains(toast)) {
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), toast);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(fe -> mainContent.getChildren().remove(toast));
                fade.play();
            }
        });
        delay.play();
    }
    
    private Button createNavButton(String text, String iconCode) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("btn-nav");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        if (iconCode != null) {
            btn.setGraphic(new FontIcon(iconCode));
            btn.setGraphicTextGap(10);
        }
        return btn;
    }
}
