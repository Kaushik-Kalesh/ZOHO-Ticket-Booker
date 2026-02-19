package org.example.llm;

import org.example.dao.MovieDAO;
import org.example.dao.MovieVoteDAO;
import org.example.dao.UserDAO;
import org.example.model.Movie;

import java.util.List;

public class PromptGenerator {

    private static final MovieVoteDAO movieVoteDAO = new MovieVoteDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final MovieDAO movieDAO = new MovieDAO();

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

        return """
        Suppose you have the following data about movies I liked and movies similar users liked:
        
        %s
        
        Follow these rules to recommend movies to me:
        - ONLY CHOOSE MOVIES SIMILAR USERS LIKED, NOT THE ONES I ALREADY LIKED.
        - ENSURE AT LEAST 5 movies are recommended.
        - The movie IDS SHOULD BE from the given data only.
        - The reason you provide should be concise and meaningful
        - Use similar users OVERLAP, and PATTERNS in genre and cast
        - Return STRICTLY valid one-line JSON
        """.formatted(sb);
    }

    public static String generateTypeRecognitionPrompt(String message) {
        return """
        You are a helpful assistant that classifies user messages into 3 categories:
        1. Movie Recommendation Requests
        2. Show Listing Request
        3. Show Booking Request
        0. Other
        
        Classify the following message and provide a concise reason for your classification:
        
        Message: "%s"
        
        Follow these rules:
        - Read the message CAREFULLY and understand the user's INTENT.
        - Return a JSON object with two properties: "reason_for_choosing_this_type" and "prompt_type".
        - "reason_for_choosing_this_type" should be a concise explanation of why you classified the message as you did.
        - "prompt_type" should be an integer (1, 2, 3, or 0) corresponding to the categories listed above.
        - Return STRICTLY valid one-line JSON
        """.formatted(message);
    }

    public static String generateMovieTitleFetchingPrompt(String message) {
        List<Movie> movies = movieDAO.getAllMovies();

        StringBuilder sb = new StringBuilder();
        for (Movie m : movies) {
            sb.append("movie_id:")
                    .append(m.id()).append(" - ")
                    .append(m.title()).append(" - ")
                    .append("\n");
        }

        return """
        Suppose you are give this list of movie titles:
        
        %s
        
        And you are given this message:
        
        "%s"
        
        Now fetch the movie title mentioned in the message, and fetch the movie_id of the movie with the MOST similar title in the given list.
        If the movie title doesn't match any title in the given list, return 0 as the movie_id.
        
        Follow these rules:
        - Return a JSON object with two properties: "reason_for_choosing_this_movie_title" and "movie_id".
        - "reason_for_choosing_this_movie_title" should be a concise explanation of why you classified the message as you did.
        - "movie_id" should be a positive integer containing the movie_id of the movie mentioned in the message.
        - Return STRICTLY valid one-line JSON
        """.formatted(sb, message);
    }

    public static String generateBookingDetailsFetchingPrompt(String message) {
        List<Movie> movies = movieDAO.getAllMovies();

        StringBuilder sb = new StringBuilder();
        for (Movie m : movies) {
            sb.append("movie_id:")
                    .append(m.id()).append(" - ")
                    .append(m.title()).append(" - ")
                    .append("\n");
        }

        return """
        Suppose you are give this list of movie titles:
        
        %s
        
        And you are given this message:
        
        "%s"
        
        Now fetch the movie title mentioned in the message, and fetch the movie_id of the movie with the MOST similar title in the given list.
        If the movie title doesn't match any title in the given list, return 0 as the movie_id.
        
        Then fetch the date mentioned in the message in the format YYYY-MM-DD. If no date is mentioned, return "N/A".
        Then fetch the time mentioned in the message in the format HH:MM. If no time is mentioned, return "N/A".
        
        Then fetch the number of seats/people mentioned in the message. If no number of seats is mentioned, return 0.
        
        Follow these rules:
        - Return a JSON object with three properties: "movie_id", "date", "time", "seats".
        - "movie_id" should be a positive integer containing the movie_id of the movie mentioned in the message.
        - "time" should be in 24 HR format
        - Return STRICTLY valid one-line JSON
        """.formatted(sb, message);
    }
}
