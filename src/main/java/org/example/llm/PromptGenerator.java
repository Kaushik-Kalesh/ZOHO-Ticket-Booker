package org.example.llm;

import org.example.dao.MovieVoteDAO;
import org.example.dao.UserDAO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ArrayNode;
import org.example.model.Movie;

import java.util.List;

public class PromptGenerator {

    private static final MovieVoteDAO movieVoteDAO = new MovieVoteDAO();
    private static final UserDAO userDAO = new UserDAO();

    public static String generateRecommendationPrompt(String username) {

        int userId = userDAO.getUser(username).id();

        List<Movie> likedMovies = movieVoteDAO.getMoviesLikedByUser(userId);
        List<Movie> similarMovies = movieVoteDAO.getMoviesLikedBySimilarUsers(userId);

        StringBuilder sb = new StringBuilder();

        sb.append("Movies I liked:\n");

        for (Movie m : likedMovies) {
            sb.append("movie_id:")
                    .append(m.id()).append(" - ")
                    .append(m.title()).append(" - ")
                    .append(m.genre()).append(" - ")
                    .append(m.director()).append(" - ")
                    .append(m.lead())
                    .append("\n");
        }

        sb.append("\n");

        sb.append("Movies Similar Users Like:\n");

        for (Movie m : similarMovies) {
            sb.append("movie_id:")
                    .append(m.id()).append(" - ")
                    .append(m.title()).append(" - ")
                    .append(m.genre()).append(" - ")
                    .append(m.director()).append(" - ")
                    .append(m.lead())
                    .append("\n");
        }

        return buildFinalPrompt(sb.toString());
    }

    private static String buildFinalPrompt(String jsonData) {

        return """
        Suppose you have the following data about movies I liked and movies similar users liked:
        
        %s
        
        Follow these rules to recommend movies to me:
        - ONLY CHOOSE MOVIES SIMILAR USERS LIKED, NOT THE ONES I ALREADY LIKED.
        - ENSURE AT LEAST 5 movies are recommended.
        - The movie IDS SHOULD BE from the given data only.
        - Do NOT recommend movies that are not in the given data.
        - The reason you provide should be concise and meaningful
        - Use similar users OVERLAP, and PATTERNS in genre and cast
        - Return STRICTLY valid one-line JSON
        """.formatted(jsonData);
    }
}
