package app.resumetor.importexport;

import app.resumetor.model.Profile;
import app.resumetor.repository.CareerDao;
import app.resumetor.repository.ProfileDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VaultManager {

    private final ObjectMapper objectMapper;
    private final ProfileDao profileDao;
    private final CareerDao careerDao;

    public VaultManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.profileDao = new ProfileDao();
        this.careerDao = new CareerDao();
    }

    public void exportVault(File destination) throws IOException {
        Map<String, Object> vault = new HashMap<>();
        vault.put("schemaVersion", 1);
        vault.put("exportedAt", System.currentTimeMillis());
        
        Profile profile = profileDao.getProfile();
        vault.put("profile", profile != null ? profile : new Profile());
        
        vault.put("experiences", careerDao.getAllExperiences());
        vault.put("projects", careerDao.getAllProjects());
        vault.put("educations", careerDao.getAllEducations());
        vault.put("skills", careerDao.getAllSkills());
        
        // Note: For full compliance, applications and generated documents should also be exported.
        // This is a minimal vault implementation for the career profile.
        
        objectMapper.writeValue(destination, vault);
    }
    
    public void importVault(File source) throws IOException {
        // Simple import without conflict resolution for this iteration
        Map<String, Object> vault = objectMapper.readValue(source, Map.class);
        
        // TODO: Map raw objects to entities and save via DAOs.
        // This requires detailed mapping or using Jackson's typerefs.
        System.out.println("Imported vault version " + vault.get("schemaVersion"));
    }
}
