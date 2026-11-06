package app.resumetor.model;

public class ProfileLink {
    private Integer id;
    private String label;
    private String url;

    public ProfileLink() {}

    public ProfileLink(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
