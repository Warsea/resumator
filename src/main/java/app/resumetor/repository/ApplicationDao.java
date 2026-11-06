package app.resumetor.repository;

import app.resumetor.model.JobApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDao {

    public static final String RESUME = "RESUME";
    public static final String COVER_LETTER = "COVER_LETTER";
    public static final String EMAIL = "EMAIL";
    public static final String JOB_ANALYSIS = "JOB_ANALYSIS";
    public static final String MATCHING = "MATCHING";

    /** Inserts the application row and returns its generated id. */
    public int insertApplication(JobApplication a) {
        String sql = "INSERT INTO application (job_title, company, job_url, location, deadline, raw_job_text) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getJobTitle());
            ps.setString(2, a.getCompany());
            ps.setString(3, a.getJobUrl());
            ps.setString(4, a.getLocation());
            ps.setString(5, a.getDeadline());
            ps.setString(6, a.getRawJobText());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getInt(1));
                    return a.getId();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public synchronized void updateTitleCompany(int id, String title, String company) {
        String sql = "UPDATE application SET job_title = COALESCE(NULLIF(job_title, ''), ?), " +
                     "company = COALESCE(NULLIF(company, ''), ?), updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, company);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Insert or replace a generated document of the given type. */
    public synchronized void saveDocument(int applicationId, String type, String content) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM generated_document WHERE application_id = ? AND type = ?")) {
                del.setInt(1, applicationId);
                del.setString(2, type);
                del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO generated_document (application_id, type, content) VALUES (?, ?, ?)")) {
                ins.setInt(1, applicationId);
                ins.setString(2, type);
                ins.setString(3, content);
                ins.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<JobApplication> getAll() {
        List<JobApplication> list = new ArrayList<>();
        String sql = "SELECT * FROM application ORDER BY created_at DESC, id DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public JobApplication getWithDocuments(int id) {
        JobApplication a = null;
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM application WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) a = map(rs);
                }
            }
            if (a == null) return null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT type, content FROM generated_document WHERE application_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String c = rs.getString("content");
                        switch (rs.getString("type")) {
                            case RESUME -> a.setResume(c);
                            case COVER_LETTER -> a.setCoverLetter(c);
                            case EMAIL -> a.setEmail(c);
                            case JOB_ANALYSIS -> a.setJobAnalysis(c);
                            case MATCHING -> a.setMatchingAnalysis(c);
                            default -> { }
                        }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return a;
    }

    public void delete(int id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM generated_document WHERE application_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM application WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private JobApplication map(ResultSet rs) throws SQLException {
        JobApplication a = new JobApplication();
        a.setId(rs.getInt("id"));
        a.setJobTitle(rs.getString("job_title"));
        a.setCompany(rs.getString("company"));
        a.setJobUrl(rs.getString("job_url"));
        a.setLocation(rs.getString("location"));
        a.setDeadline(rs.getString("deadline"));
        a.setRawJobText(rs.getString("raw_job_text"));
        a.setCreatedAt(rs.getString("created_at"));
        return a;
    }
}
