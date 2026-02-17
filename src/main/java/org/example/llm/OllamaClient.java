package org.example.llm;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class OllamaClient {

    public static String generate(String prompt) throws URISyntaxException, IOException {

        URI uri = new URI("http://localhost:11434/api/generate");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String body = """
        {
          "model": "phi",
          "prompt": %s,
          "stream": false
        }
        """.formatted(escapeJson(prompt));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        Map<String, Object> response = new ObjectMapper().readValue(conn.getInputStream(), Map.class);
        return response.get("response").toString();
    }

    private static String escapeJson(String input) {
        return "\"" + input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}
