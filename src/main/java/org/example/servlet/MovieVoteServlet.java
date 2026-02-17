package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.dao.MovieVoteDAO;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@WebServlet("/vote_movie")
public class MovieVoteServlet extends HttpServlet {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MovieVoteDAO movieVoteDAO = new MovieVoteDAO();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
            int movie_id = (int) payload.get("movie_id");
            int user_id = (int) payload.get("user_id");

            movieVoteDAO.addVote(movie_id, user_id);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }
    }
}
