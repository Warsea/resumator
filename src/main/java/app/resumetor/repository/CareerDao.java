package app.resumetor.repository;

import app.resumetor.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CareerDao {

    // --- EXPERIENCE ---
    public List<Experience> getAllExperiences() {
        List<Experience> list = new ArrayList<>();
        String sql = "SELECT * FROM experience ORDER BY start_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Experience e = new Experience();
                e.setId(rs.getInt("id"));
                e.setCompany(rs.getString("company"));
                e.setRole(rs.getString("role"));
                e.setEmploymentType(rs.getString("employment_type"));
                e.setLocation(rs.getString("location"));
                e.setStartDate(rs.getString("start_date"));
                e.setEndDate(rs.getString("end_date"));
                e.setCurrent(rs.getBoolean("is_current"));
                e.setDescription(rs.getString("description"));
                e.setResponsibilities(rs.getString("responsibilities"));
                e.setAchievements(rs.getString("achievements"));
                e.setTechnologies(rs.getString("technologies"));
                e.setSkills(rs.getString("skills"));
                e.setNotes(rs.getString("notes"));
                list.add(e);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    // --- PROJECT ---
    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project ORDER BY start_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Project p = new Project();
                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setDescription(rs.getString("description"));
                p.setRole(rs.getString("role"));
                p.setTechnologies(rs.getString("technologies"));
                p.setAchievements(rs.getString("achievements"));
                p.setUrl(rs.getString("url"));
                p.setStartDate(rs.getString("start_date"));
                p.setEndDate(rs.getString("end_date"));
                p.setNotes(rs.getString("notes"));
                list.add(p);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    // --- EDUCATION ---
    public List<Education> getAllEducations() {
        List<Education> list = new ArrayList<>();
        String sql = "SELECT * FROM education ORDER BY start_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Education e = new Education();
                e.setId(rs.getInt("id"));
                e.setInstitution(rs.getString("institution"));
                e.setDegree(rs.getString("degree"));
                e.setField(rs.getString("field"));
                e.setStartDate(rs.getString("start_date"));
                e.setEndDate(rs.getString("end_date"));
                e.setCurrent(rs.getBoolean("is_current"));
                e.setGpa(rs.getString("gpa"));
                e.setCoursework(rs.getString("coursework"));
                e.setAchievements(rs.getString("achievements"));
                e.setActivities(rs.getString("activities"));
                e.setNotes(rs.getString("notes"));
                list.add(e);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }

    // --- SKILL ---
    public List<Skill> getAllSkills() {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM skill ORDER BY category, name";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Skill s = new Skill();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setCategory(rs.getString("category"));
                s.setProficiency(rs.getString("proficiency"));
                s.setYearsExperience(rs.getString("years_experience"));
                s.setNotes(rs.getString("notes"));
                list.add(s);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return list;
    }
    
    // --- SAVE METHODS (insert when id is null, update otherwise) ---
    public void saveExperience(Experience e) {
        String sql = e.getId() == null
                ? "INSERT INTO experience (company, role, start_date, end_date, description) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE experience SET company = ?, role = ?, start_date = ?, end_date = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getCompany());
            pstmt.setString(2, e.getRole());
            pstmt.setString(3, e.getStartDate());
            pstmt.setString(4, e.getEndDate());
            pstmt.setString(5, e.getDescription());
            if (e.getId() != null) pstmt.setInt(6, e.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void saveProject(Project p) {
        String sql = p.getId() == null
                ? "INSERT INTO project (title, description, url) VALUES (?, ?, ?)"
                : "UPDATE project SET title = ?, description = ?, url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getTitle());
            pstmt.setString(2, p.getDescription());
            pstmt.setString(3, p.getUrl());
            if (p.getId() != null) pstmt.setInt(4, p.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void saveEducation(Education e) {
        String sql = e.getId() == null
                ? "INSERT INTO education (institution, degree, field, start_date, end_date) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE education SET institution = ?, degree = ?, field = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getInstitution());
            pstmt.setString(2, e.getDegree());
            pstmt.setString(3, e.getField());
            pstmt.setString(4, e.getStartDate());
            pstmt.setString(5, e.getEndDate());
            if (e.getId() != null) pstmt.setInt(6, e.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void saveSkill(Skill s) {
        String sql = s.getId() == null
                ? "INSERT INTO skill (name, proficiency) VALUES (?, ?)"
                : "UPDATE skill SET name = ?, proficiency = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getName());
            pstmt.setString(2, s.getProficiency());
            if (s.getId() != null) pstmt.setInt(3, s.getId());
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    // --- DELETE METHODS ---
    public void deleteExperience(int id) {
        String sql = "DELETE FROM experience WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    public void deleteProject(int id) {
        String sql = "DELETE FROM project WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    public void deleteEducation(int id) {
        String sql = "DELETE FROM education WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
    
    public void deleteSkill(int id) {
        String sql = "DELETE FROM skill WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
