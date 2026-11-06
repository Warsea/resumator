package app.resumetor.model;

/** A saved job application along with its generated documents. */
public class JobApplication {
    private Integer id;
    private String jobTitle;
    private String company;
    private String jobUrl;
    private String location;
    private String deadline;
    private String rawJobText;
    private String createdAt;

    private String jobAnalysis;
    private String matchingAnalysis;
    private String resume;
    private String coverLetter;
    private String email;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getJobUrl() { return jobUrl; }
    public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public String getRawJobText() { return rawJobText; }
    public void setRawJobText(String rawJobText) { this.rawJobText = rawJobText; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getJobAnalysis() { return jobAnalysis; }
    public void setJobAnalysis(String jobAnalysis) { this.jobAnalysis = jobAnalysis; }
    public String getMatchingAnalysis() { return matchingAnalysis; }
    public void setMatchingAnalysis(String matchingAnalysis) { this.matchingAnalysis = matchingAnalysis; }
    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() {
        String t = jobTitle != null && !jobTitle.isBlank() ? jobTitle : "Untitled Role";
        return company != null && !company.isBlank() ? t + " @ " + company : t;
    }
}
