package app.resumetor.export;

import app.resumetor.model.Education;
import app.resumetor.model.Profile;
import app.resumetor.model.ProfileLink;
import app.resumetor.util.JsonText;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts LLM output into a simple, ATS-friendly markup used by the preview and exporters:
 * <pre>
 * # Heading (name)        ## Section heading       ### Entry heading
 * - bullet                plain line = paragraph   blank line = spacing
 * </pre>
 */
public final class DocumentFormatter {

    private DocumentFormatter() {}

    public static String resumeToMarkup(String llmOutput, Profile profile, List<ProfileLink> links, List<Education> educations) {
        JsonNode root = JsonText.parse(llmOutput);
        if (root == null) {
            // Model didn't return JSON; just prepend header to whatever it wrote.
            return header(profile, links) + "\n" + (llmOutput == null ? "" : llmOutput.trim());
        }

        StringBuilder sb = new StringBuilder(header(profile, links));

        String summary = JsonText.text(root, "professionalSummary");
        if (!summary.isEmpty()) {
            sb.append("\n## Professional Summary\n").append(summary).append("\n");
        }

        JsonNode skills = root.path("skills");
        if (skills.isArray() && !skills.isEmpty()) {
            List<String> names = new ArrayList<>();
            skills.forEach(s -> names.add(s.asText().trim()));
            sb.append("\n## Skills\n").append(String.join(", ", names)).append("\n");
        }

        JsonNode exps = root.path("experiences");
        if (exps.isArray() && !exps.isEmpty()) {
            sb.append("\n## Experience\n");
            for (JsonNode e : exps) {
                sb.append("### ").append(joinNonEmpty(" | ",
                        JsonText.text(e, "role"), JsonText.text(e, "company"), JsonText.text(e, "dates"))).append("\n");
                e.path("bullets").forEach(b -> sb.append("- ").append(b.asText().trim()).append("\n"));
            }
        }

        JsonNode projects = root.path("projects");
        if (projects.isArray() && !projects.isEmpty()) {
            sb.append("\n## Projects\n");
            for (JsonNode p : projects) {
                sb.append("### ").append(JsonText.text(p, "title")).append("\n");
                JsonNode bullets = p.path("bullets");
                if (bullets.isArray() && !bullets.isEmpty()) {
                    bullets.forEach(b -> sb.append("- ").append(b.asText().trim()).append("\n"));
                } else {
                    String d = JsonText.text(p, "description");
                    if (!d.isEmpty()) sb.append("- ").append(d).append("\n");
                }
            }
        }

        // Education comes straight from the profile: it's factual and needs no tailoring.
        if (educations != null && !educations.isEmpty()) {
            sb.append("\n## Education\n");
            for (Education ed : educations) {
                String degree = joinNonEmpty(" in ", nz(ed.getDegree()), nz(ed.getField()));
                String dates = joinNonEmpty(" - ", nz(ed.getStartDate()), nz(ed.getEndDate()));
                sb.append("### ").append(joinNonEmpty(" | ", degree, nz(ed.getInstitution()), dates)).append("\n");
            }
        }
        return sb.toString().trim() + "\n";
    }

    public static String coverLetterToMarkup(String llmOutput, Profile profile, List<ProfileLink> links) {
        String body = plainText(llmOutput, "coverLetter", "body", "content", "letter");
        return header(profile, links) + "\n" + LocalDate.now() + "\n\n" + body.trim() + "\n";
    }

    /** Extracts plain text even if the model wrapped it in JSON. */
    public static String plainText(String llmOutput, String... candidateFields) {
        if (llmOutput == null) return "";
        JsonNode root = JsonText.parse(llmOutput);
        if (root != null && llmOutput.trim().startsWith("{")) {
            for (String f : candidateFields) {
                String v = JsonText.text(root, f);
                if (!v.isEmpty()) return v;
            }
        }
        return llmOutput.trim();
    }

    private static String header(Profile profile, List<ProfileLink> links) {
        StringBuilder sb = new StringBuilder();
        if (profile == null) return "";
        sb.append("# ").append(nz(profile.getFullName())).append("\n");
        if (!nz(profile.getCurrentTitle()).isEmpty()) sb.append(profile.getCurrentTitle()).append("\n");
        List<String> contact = new ArrayList<>();
        if (!nz(profile.getEmail()).isEmpty()) contact.add(profile.getEmail());
        if (!nz(profile.getPhone()).isEmpty()) contact.add(profile.getPhone());
        if (links != null) {
            for (ProfileLink l : links) contact.add(l.getLabel() + ": " + l.getUrl());
        }
        if (!contact.isEmpty()) sb.append(String.join(" | ", contact)).append("\n");
        return sb.toString();
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }

    private static String joinNonEmpty(String sep, String... parts) {
        List<String> out = new ArrayList<>();
        for (String p : parts) if (p != null && !p.isBlank()) out.add(p.trim());
        return String.join(sep, out);
    }
}
