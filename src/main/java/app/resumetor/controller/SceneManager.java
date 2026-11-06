package app.resumetor.controller;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static void showOnboarding() {
        OnboardingLayout onboarding = new OnboardingLayout();
        Scene scene = new Scene(onboarding, 1024, 768);
        scene.getStylesheets().add(SceneManager.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }
    
    public static void showMainApp() {
        showMainApp(false);
    }

    public static void showMainApp(boolean fromOnboarding) {
        MainLayout mainLayout = new MainLayout();
        Scene scene = new Scene(mainLayout, 1024, 768);
        scene.getStylesheets().add(SceneManager.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);

        if (fromOnboarding) {
            mainLayout.showToast("Basic profile saved! Please complete your Career Profile (Experience, Projects, Education, Skills) before generating applications.");
        }
    }
}
