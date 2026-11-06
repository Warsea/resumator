package app.resumetor.model;

public class Education {
    private Integer id;
    private String institution;
    private String degree;
    private String field;
    private String startDate;
    private String endDate;
    private boolean isCurrent;
    private String gpa;
    private String coursework;
    private String achievements;
    private String activities;
    private String notes;

    public Education() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }

    public String getGpa() { return gpa; }
    public void setGpa(String gpa) { this.gpa = gpa; }

    public String getCoursework() { return coursework; }
    public void setCoursework(String coursework) { this.coursework = coursework; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }

    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
