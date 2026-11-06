package app.resumetor.model;

public class Skill {
    private Integer id;
    private String name;
    private String category;
    private String proficiency;
    private String yearsExperience;
    private String notes;

    public Skill() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProficiency() { return proficiency; }
    public void setProficiency(String proficiency) { this.proficiency = proficiency; }

    public String getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(String yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
