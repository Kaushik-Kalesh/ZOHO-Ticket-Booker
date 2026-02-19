package org.example.llm;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class OllamaClient {

    private static final String MODEL = "phi3:mini";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static InputStream generate(String prompt, String format)
            throws URISyntaxException, IOException {

        URI uri = new URI("http://localhost:11434/api/generate");
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JsonNode formatNode = mapper.readTree(format);

        ObjectNode root = mapper.createObjectNode();
        root.put("model", MODEL);
        root.set("format", formatNode);
        root.put("prompt", prompt);
        root.put("stream", false);

        String body = mapper.writeValueAsString(root);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        }

        return conn.getInputStream();
    }
}
