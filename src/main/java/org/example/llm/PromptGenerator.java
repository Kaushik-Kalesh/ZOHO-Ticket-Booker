package org.example.llm;

import org.example.dao.MovieVoteDAO;
import org.example.dao.UserDAO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;
import org.example.model.Movie;

import java.util.List;

public class PromptGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MovieVoteDAO movieVoteDAO = new MovieVoteDAO();
    private static final UserDAO userDAO = new UserDAO();

    public static String generateRecommendationPrompt(String username) {
        int userId = userDAO.getUser(username).id();
        List<Movie> likedMovies = movieVoteDAO.getMoviesLikedByUser(userId);
        List<Movie> similarMovies = movieVoteDAO.getMoviesLikedBySimilarUsers(userId);
        List<Movie> candidateMovies = movieVoteDAO.getCandidateMovies(userId);

        ObjectNode root = mapper.createObjectNode();

        ArrayNode likedArray = root.putArray("liked_movies");
        for (Movie m : likedMovies) {
            ObjectNode movieNode = likedArray.addObject();
            movieNode.put("id", m.id());
            movieNode.put("title", m.title());
            movieNode.put("genre", m.genre());
            movieNode.put("director", m.director());
            movieNode.put("lead", m.lead());
        }

        ArrayNode similarArray = root.putArray("movies_liked_by_similar_users");
        for (Movie m : similarMovies) {
            ObjectNode movieNode = similarArray.addObject();
            movieNode.put("id", m.id());
            movieNode.put("title", m.title());
            movieNode.put("genre", m.genre());
            movieNode.put("director", m.director());
            movieNode.put("lead", m.lead());
        }

        ArrayNode candidateArray = root.putArray("candidate_movies");
        for (Movie m : candidateMovies) {
            ObjectNode movieNode = candidateArray.addObject();
            movieNode.put("id", m.id());
            movieNode.put("title", m.title());
            movieNode.put("genre", m.genre());
            movieNode.put("director", m.director());
            movieNode.put("lead", m.lead());
        }

        String structuredData = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(root);

        return buildFinalPrompt(structuredData);
    }

    private static String buildFinalPrompt(String jsonData) {

        return """
Based on the following JSON data:

%s

Rules:
1. Recommend 10 movies from candidate_movies only.
2. Do NOT invent movies.
3. Use similar_users overlap and genre patterns.
4. Return strictly valid JSON in this format:

{
  "recommendations": [
    {
      "movie_id": number,
      "reason": "short explanation"
    }
  ]
}
""".formatted(jsonData);
    }
}
