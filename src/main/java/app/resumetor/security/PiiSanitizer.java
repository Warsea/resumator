package app.resumetor.security;

import app.resumetor.model.Profile;
import app.resumetor.model.Experience;
import app.resumetor.model.Project;
import app.resumetor.model.Education;
import app.resumetor.model.Skill;

import java.util.List;

public class PiiSanitizer {

    public static String buildSanitizedProfileContext(
            Profile profile, 
            List<Experience> experiences,
            List<Project> projects,
            List<Education> educations,
            List<Skill> skills) {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("# Candidate Profile\n\n");
        sb.append("Name: [NAME]\n");
        sb.append("Email: [EMAIL]\n");
        sb.append("Phone: [PHONE]\n");
        sb.append("Address: [ADDRESS]\n");
        sb.append("LinkedIn: [LINKEDIN]\n");
        sb.append("GitHub: [GITHUB]\n");
        sb.append("Portfolio: [PORTFOLIO]\n\n");
        
        if (profile != null) {
            if (profile.getCurrentTitle() != null) sb.append("Current Title: ").append(profile.getCurrentTitle()).append("\n");
            if (profile.getCareerDirection() != null) sb.append("Career Direction: ").append(profile.getCareerDirection()).append("\n");
            if (profile.getProfessionalSummary() != null) sb.append("Summary: ").append(profile.getProfessionalSummary()).append("\n\n");
        }
        
        if (experiences != null && !experiences.isEmpty()) {
            sb.append("## Experience\n");
            for (Experience ex : experiences) {
                sb.append("### ").append(ex.getRole()).append(" at ").append(ex.getCompany()).append("\n");
                sb.append("Dates: ").append(ex.getStartDate()).append(" - ").append(ex.isCurrent() ? "Present" : ex.getEndDate()).append("\n");
                if (ex.getDescription() != null) sb.append("Description: ").append(ex.getDescription()).append("\n");
                if (ex.getResponsibilities() != null) sb.append("Responsibilities:\n").append(ex.getResponsibilities()).append("\n");
                if (ex.getAchievements() != null) sb.append("Achievements:\n").append(ex.getAchievements()).append("\n");
                sb.append("\n");
            }
        }
        
        if (projects != null && !projects.isEmpty()) {
            sb.append("## Projects\n");
            for (Project p : projects) {
                sb.append("### ").append(p.getTitle()).append("\n");
                if (p.getRole() != null) sb.append("Role: ").append(p.getRole()).append("\n");
                if (p.getDescription() != null) sb.append("Description: ").append(p.getDescription()).append("\n");
                if (p.getTechnologies() != null) sb.append("Technologies: ").append(p.getTechnologies()).append("\n");
                if (p.getAchievements() != null) sb.append("Achievements: ").append(p.getAchievements()).append("\n");
                sb.append("\n");
            }
        }
        
        if (educations != null && !educations.isEmpty()) {
            sb.append("## Education\n");
            for (Education ed : educations) {
                sb.append("### ").append(ed.getDegree()).append(" in ").append(ed.getField()).append(" - ").append(ed.getInstitution()).append("\n");
                sb.append("Dates: ").append(ed.getStartDate()).append(" - ").append(ed.getEndDate()).append("\n");
                if (ed.getGpa() != null) sb.append("GPA: ").append(ed.getGpa()).append("\n");
                sb.append("\n");
            }
        }
        
        if (skills != null && !skills.isEmpty()) {
            sb.append("## Skills\n");
            for (Skill s : skills) {
                sb.append("- ").append(s.getName());
                if (s.getProficiency() != null && !s.getProficiency().isEmpty()) {
                    sb.append(" (").append(s.getProficiency()).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    public static String injectRealPii(String content, Profile profile) {
        if (content == null || profile == null) return content;
        
        return content
                .replace("[NAME]", profile.getFullName() != null ? profile.getFullName() : "")
                .replace("[EMAIL]", profile.getEmail() != null ? profile.getEmail() : "")
                .replace("[PHONE]", profile.getPhone() != null ? profile.getPhone() : "")
                .replace("[ADDRESS]", profile.getAddress() != null ? profile.getAddress() : "")
                .replace("[LINKEDIN]", profile.getLinkedinUrl() != null ? profile.getLinkedinUrl() : "")
                .replace("[GITHUB]", profile.getGithubUrl() != null ? profile.getGithubUrl() : "")
                .replace("[PORTFOLIO]", profile.getPortfolioUrl() != null ? profile.getPortfolioUrl() : "");
    }
}
