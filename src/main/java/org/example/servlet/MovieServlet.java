package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.dao.MovieDAO;
import org.example.llm.OllamaClient;
import org.example.llm.PromptGenerator;
import org.example.model.Movie;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/movies")
public class MovieServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final MovieDAO movieDAO = new MovieDAO();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
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
            String prompt = PromptGenerator.generateRecommendationPrompt(session.getAttribute("user").toString());
            InputStream responseStream = OllamaClient.generate(prompt);

//            IO.println(new ObjectMapper().readValue(responseStream, Map.class).get("response"));

            /*
            * Parse Ollama response
            * */
            String responseJson = mapper.readTree(responseStream).get("response").asString();

            JsonNode responseNode = mapper.readTree(responseJson);

            List<Integer> movieIds = new ArrayList<>();
            for (JsonNode node : responseNode.get("movie_ids")) {
                movieIds.add(node.asInt());
            }

            /*
            * Build recommended and other movies lists
            * */
            Set<Integer> recommendedIdSet = new HashSet<>(movieIds);

            int LIMIT = 5;
            List<Movie> recommendedMovies = new ArrayList<>(LIMIT);
            List<Movie> otherMovies = new ArrayList<>();

            List<Movie> movies = movieDAO.getAllMovies();
            for (Movie movie : movies) {
                if (recommendedMovies.size() < LIMIT && recommendedIdSet.contains(movie.id())) {
                    recommendedMovies.add(movie);
                } else {
                    otherMovies.add(movie);
                }
            }

            if (recommendedMovies.size() < LIMIT) {
                int needed = LIMIT - recommendedMovies.size();
                int toMove = Math.min(needed, otherMovies.size());

                List<Movie> moved = new ArrayList<>(otherMovies.subList(0, toMove));

                recommendedMovies.addAll(moved);
                otherMovies.subList(0, toMove).clear();
            }

            Map<String, List<Movie>> result = new HashMap<>();
            result.put("recommended_movies", recommendedMovies);
            result.put("other_movies", otherMovies);

            mapper.writeValue(response.getWriter(), result);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
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
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace();
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }
    }
}
