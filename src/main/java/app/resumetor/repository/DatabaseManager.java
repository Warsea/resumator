package app.resumetor.repository;

import app.resumetor.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String DB_FILE = resolveDbFile();
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static String resolveDbFile() {
        File localDir = new File("data");
        if (localDir.exists() && localDir.isDirectory() && localDir.canWrite()) {
            return "data/resumetor.db";
        }
        String userHome = System.getProperty("user.home", ".");
        File userAppDir = new File(userHome, ".resumetor");
        if (!userAppDir.exists()) {
            userAppDir.mkdirs();
        }
        return new File(userAppDir, "resumetor.db").getAbsolutePath();
    }

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            log.error("Failed to get database connection", e);
        }
        return connection;
    }

    private void initDatabase() {
        File dbFile = new File(DB_FILE);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
             
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");
            
            // Read schema from resources
            String schema = readSchemaFile();
            if (schema != null && !schema.isEmpty()) {
                String[] statements = schema.split(";");
                for (String s : statements) {
                    if (!s.trim().isEmpty()) {
                        stmt.execute(s.trim());
                    }
                }
                log.info("Database schema initialized successfully.");
            }
        } catch (SQLException e) {
            log.error("Failed to initialize database", e);
        }
    }

    private String readSchemaFile() {
        try (InputStream in = getClass().getResourceAsStream("/db/schema.sql")) {
            if (in == null) {
                log.error("schema.sql not found in resources!");
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.error("Failed to read schema file", e);
            return null;
        }
    }
}
