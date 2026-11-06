package app.resumetor;

import app.resumetor.controller.SceneManager;
import app.resumetor.model.Profile;
import app.resumetor.repository.DatabaseManager;
import app.resumetor.repository.ProfileDao;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.InputStream;

public class App extends Application {

    @Override
    public void init() throws Exception {
        // Initialize Database
        DatabaseManager.getInstance().getConnection();
    }

    @Override
    public void start(Stage stage) {
        SceneManager.setPrimaryStage(stage);
        
        stage.setTitle("Resumetor - AI Job Application Assistant");
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // --- 2. Load and set the app icon ---
        try (InputStream iconStream = getClass().getResourceAsStream("/images/icon.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("App icon not found at /images/icon.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ------------------------------------
        
        ProfileDao profileDao = new ProfileDao();
        Profile profile = profileDao.getProfile();
        
        if (profile == null) {
            SceneManager.showOnboarding();
        } else {
            SceneManager.showMainApp();
        }
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
