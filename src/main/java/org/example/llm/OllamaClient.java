package org.example.llm;

import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class OllamaClient {

    private static final String MODEL = "phi";

    public static InputStream generate(String prompt) throws URISyntaxException, IOException {

        URI uri = new URI("http://localhost:11434/api/generate");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        String body = """
        {
         "model": "%s",
         "format": {
             "type": "object",
             "properties": {
                 "reason_for_choosing_these_specific_movies": {"type": "string"},
                 "movie_ids": {
                     "type": "array",
                     "items": {"type": "number"}
                 }
             },
             "required": ["reason_for_choosing_these_specific_movies", "movie_ids"]
         },
         "prompt": %s,
         "stream": false
        }
        """.formatted(MODEL, escapeJson(prompt));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        return conn.getInputStream();
    }

    private static String escapeJson(String input) {
        return "\"" + input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n") + "\"";
    }
}
