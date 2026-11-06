package app.resumetor.controller;

import app.resumetor.model.Profile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OnboardingLayout extends ScrollPane {

    private final VBox container;
    private final Profile profile;

    public OnboardingLayout() {
        this.profile = new Profile();
        
        container = new VBox(20);
        container.setPadding(new Insets(40));
        container.setAlignment(Pos.TOP_CENTER);
        container.setMaxWidth(800);
        
        this.setContent(container);
        this.setFitToWidth(true);
        this.getStyleClass().add("main-content");
        
        buildUi();
    }

    private void buildUi() {
        Label title = new Label("Welcome to Resumetor");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("Build your career profile once. We'll use it to tailor your applications for different jobs.");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: -text-secondary;");
        subtitle.setWrapText(true);
        
        VBox header = new VBox(10, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 30, 0));

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);
        
        // Form Fields
        TextField nameField = createField(form, "Full Name", 0);
        TextField emailField = createField(form, "Email", 1);
        TextField phoneField = createField(form, "Phone Number", 2);
        TextField addressField = createField(form, "Address / Location", 3);
        TextField titleField = createField(form, "Current Job Title", 4);
        TextField directionField = createField(form, "Desired Career Direction", 5);
        
        Label privacyNotice = new Label("Your contact information is stored locally and is NOT shared with the AI provider.");
        privacyNotice.setStyle("-fx-font-size: 12px; -fx-text-fill: -success;");
        
        Button nextBtn = new Button("Complete Basic Profile");
        nextBtn.getStyleClass().addAll("btn", "btn-primary");
        nextBtn.setOnAction(e -> {
            // Save basic info
            profile.setFullName(nameField.getText());
            profile.setEmail(emailField.getText());
            profile.setPhone(phoneField.getText());
            profile.setAddress(addressField.getText());
            profile.setCurrentTitle(titleField.getText());
            profile.setCareerDirection(directionField.getText());
            
            app.resumetor.repository.ProfileDao profileDao = new app.resumetor.repository.ProfileDao();
            profileDao.saveProfile(profile);
            
            // Move to main app and show onboarding completion toast
            app.resumetor.controller.SceneManager.showMainApp(true);
        });
        
        HBox actions = new HBox(nextBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 0, 0, 0));

        container.getChildren().addAll(header, form, privacyNotice, actions);
    }
    
    private TextField createField(GridPane grid, String labelText, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-weight: bold;");
        TextField field = new TextField();
        field.setPrefWidth(400);
        field.setStyle("-fx-padding: 8px; -fx-background-radius: 4px; -fx-border-color: -border-color; -fx-border-radius: 4px;");
        
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        
        return field;
    }
}
