package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.dao.MovieDAO;
import org.example.model.Movie;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/movies")
public class MovieServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final MovieDAO movieDAO = new MovieDAO();

    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if ("ids".equals(request.getParameter("type"))) {

            try {
                mapper.writeValue(response.getWriter(),
                        Map.of("movie_ids",
                                movieDAO.getAllMovies().stream()
                                        .map(Movie::id)
                                        .toList()
                        )
                );
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
            }

            return;
        }

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Not authenticated"));
            return;
        }

        try {
            mapper.writeValue(response.getWriter(), Map.of("movies", movieDAO.getAllMovies()));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }

    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
            String title = (String) payload.get("title");
            String director = (String) payload.get("director");
            String lead = (String) payload.get("lead");
            String genre = (String) payload.get("genre");
            LocalDate releaseDate = LocalDate.parse((String) payload.get("releaseDate"));

            movieDAO.addMovie(title, director, lead, genre, releaseDate);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }
    }
}
