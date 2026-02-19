package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.util.ChatRAG;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void doGet(HttpServletRequest request,
                       HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Not authenticated"));
            return;
        }

        try {
            Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
            String message = (String) payload.get("message");
            String reply = "";

            int promptType = ChatRAG.getPromptType(message);
            if (promptType == 1) {
                reply = ChatRAG.getRecommendedMovies(
                        session.getAttribute("user")
                                .toString()
                );
            } else if (promptType == 2) {
                reply = ChatRAG.getShowListings(message);
            } else if (promptType == 3) {
                reply = ChatRAG.bookShow(session.getAttribute("user").toString(), message);
            } else {
                reply = "Sorry, I couldn't understand your request.";
            }

            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getWriter(),
                    Map.of("reply", reply)
            );
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }
    }
}
