package app.resumetor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Helpers for dealing with LLM output that is supposed to be JSON. */
public final class JsonText {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonText() {}

    /** Returns parsed JSON, tolerating markdown code fences and leading/trailing chatter. Null if unparseable. */
    public static JsonNode parse(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            s = nl >= 0 ? s.substring(nl + 1) : s;
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            return MAPPER.readTree(s.substring(start, end + 1));
        } catch (Exception e) {
            return null;
        }
    }

    public static String text(JsonNode node, String field) {
        if (node == null) return "";
        JsonNode v = node.path(field);
        return v.isMissingNode() || v.isNull() ? "" : v.asText("").trim();
    }

    /** "missingRequirements" -> "Missing Requirements" */
    public static String humanize(String camel) {
        String spaced = camel.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String w : spaced.split(" ")) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
