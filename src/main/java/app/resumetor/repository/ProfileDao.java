package app.resumetor.repository;

import app.resumetor.model.Profile;
import app.resumetor.model.ProfileLink;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfileDao {

    public Profile getProfile() {
        String sql = "SELECT * FROM user_profile ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            if (rs.next()) {
                return mapToProfile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveProfile(Profile profile) {
        if (profile.getId() == null) {
            insertProfile(profile);
        } else {
            updateProfile(profile);
        }
    }

    private void insertProfile(Profile profile) {
        String sql = "INSERT INTO user_profile (full_name, email, phone, address, linkedin_url, " +
                     "github_url, portfolio_url, current_title, career_direction, professional_summary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            setProfileParams(pstmt, profile);
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateProfile(Profile profile) {
        String sql = "UPDATE user_profile SET full_name = ?, email = ?, phone = ?, address = ?, " +
                     "linkedin_url = ?, github_url = ?, portfolio_url = ?, current_title = ?, " +
                     "career_direction = ?, professional_summary = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            setProfileParams(pstmt, profile);
            pstmt.setInt(11, profile.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void setProfileParams(PreparedStatement pstmt, Profile profile) throws SQLException {
        pstmt.setString(1, profile.getFullName());
        pstmt.setString(2, profile.getEmail());
        pstmt.setString(3, profile.getPhone());
        pstmt.setString(4, profile.getAddress());
        pstmt.setString(5, profile.getLinkedinUrl());
        pstmt.setString(6, profile.getGithubUrl());
        pstmt.setString(7, profile.getPortfolioUrl());
        pstmt.setString(8, profile.getCurrentTitle());
        pstmt.setString(9, profile.getCareerDirection());
        pstmt.setString(10, profile.getProfessionalSummary());
    }
    
    // --- LINKS ---
    public List<ProfileLink> getLinks() {
        List<ProfileLink> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM profile_link ORDER BY id")) {
            while (rs.next()) {
                ProfileLink l = new ProfileLink(rs.getString("label"), rs.getString("url"));
                l.setId(rs.getInt("id"));
                list.add(l);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Replaces all saved links with the given list. */
    public void replaceLinks(List<ProfileLink> links) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM profile_link");
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO profile_link (label, url) VALUES (?, ?)")) {
                for (ProfileLink l : links) {
                    if (l.getUrl() == null || l.getUrl().isBlank()) continue;
                    ps.setString(1, l.getLabel() == null || l.getLabel().isBlank() ? "Link" : l.getLabel().trim());
                    ps.setString(2, l.getUrl().trim());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Profile mapToProfile(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setId(rs.getInt("id"));
        profile.setFullName(rs.getString("full_name"));
        profile.setEmail(rs.getString("email"));
        profile.setPhone(rs.getString("phone"));
        profile.setAddress(rs.getString("address"));
        profile.setLinkedinUrl(rs.getString("linkedin_url"));
        profile.setGithubUrl(rs.getString("github_url"));
        profile.setPortfolioUrl(rs.getString("portfolio_url"));
        profile.setCurrentTitle(rs.getString("current_title"));
        profile.setCareerDirection(rs.getString("career_direction"));
        profile.setProfessionalSummary(rs.getString("professional_summary"));
        return profile;
    }
}
