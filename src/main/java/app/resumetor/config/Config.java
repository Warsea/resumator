package app.resumetor.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            log.warn("Failed to load .env file. Falling back to system environment variables.");
        }
    }

    public static String get(String key) {
        if (dotenv != null) {
            String val = dotenv.get(key);
            if (val != null) {
                return val;
            }
        }
        return System.getenv(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }
}
