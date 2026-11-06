package app.resumetor.ai;

import app.resumetor.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OpenRouterLLMService {
    private static final Logger log = LoggerFactory.getLogger(OpenRouterLLMService.class);
    private static final String DEFAULT_MODEL = "openrouter/free"; // Or any standard model
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public OpenRouterLLMService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = Config.get("OPENROUTER_API_KEY", "");
        
        if (this.apiKey.isEmpty()) {
            log.warn("OPENROUTER_API_KEY is not set. AI requests will fail.");
        }
    }

    public CompletableFuture<String> sendRequestAsync(String systemPrompt, String userPrompt) {
        return sendRequestAsync(systemPrompt, userPrompt, false);
    }

    public CompletableFuture<String> sendRequestAsync(String systemPrompt, String userPrompt, boolean jsonMode) {
        if (apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("API Key is missing"));
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", Config.get("AI_MODEL", DEFAULT_MODEL));
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            
            requestBody.put("messages", List.of(systemMessage, userMessage));
            if (jsonMode) {
                Map<String, Object> responseFormat = new HashMap<>();
                responseFormat.put("type", "json_object");
                requestBody.put("response_format", responseFormat);
            }

            String jsonString = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "http://localhost:8080") // OpenRouter requirement
                    .header("X-Title", "Resumetor") // OpenRouter requirement
                    .timeout(Duration.ofSeconds(60)) // Generation can take time
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return extractContentFromResponse(response.body());
                        } else {
                            log.error("API Error: {} - {}", response.statusCode(), response.body());
                            throw new RuntimeException("API returned status: " + response.statusCode());
                        }
                    });

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private String extractContentFromResponse(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON response", e);
        }
        return null;
    }
}
