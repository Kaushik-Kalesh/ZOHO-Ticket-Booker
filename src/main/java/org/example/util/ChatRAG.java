package org.example.util;

import org.example.dao.BookingDAO;
import org.example.dao.MovieDAO;
import org.example.dao.ShowDAO;
import org.example.dao.UserDAO;
import org.example.llm.OllamaClient;
import org.example.llm.PromptGenerator;
import org.example.model.Movie;
import org.example.model.Show;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatRAG {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserDAO userDAO = new UserDAO();
    private static final MovieDAO movieDAO = new MovieDAO();
    private static final ShowDAO showDAO = new ShowDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();

    public static int getPromptType(String message)
            throws URISyntaxException, IOException {
        String prompt = PromptGenerator.generateTypeRecognitionPrompt(message);
        String format = """
        {
          "type": "object",
          "properties": {
            "reason_for_choosing_this_type": {
              "type": "string"
            },
            "prompt_type": {
                "type": "integer"
            }
          },
          "required": [
            "reason_for_choosing_this_type",
            "prompt_type"
          ]
        }
        """;

//            IO.println(new ObjectMapper().readValue(responseStream, Map.class).get("response"));

        /*
         * Parse Ollama response
         * */
        InputStream responseStream = OllamaClient.generate(prompt, format);
        String responseJson = mapper.readTree(responseStream).get("response").asString();

        JsonNode responseNode = mapper.readTree(responseJson);

        return responseNode.get("prompt_type").asInt();
    }

    public static String getRecommendedMovies(String username)
            throws URISyntaxException, IOException {
        String prompt = PromptGenerator.generateRecommendationPrompt(username);
        String format = """
        {
          "type": "object",
          "properties": {
            "reason_for_choosing_these_specific_movies": {
              "type": "string"
            },
            "movie_ids": {
              "type": "array",
              "items": {
                "type": "integer"
              }
            }
          },
          "required": [
            "reason_for_choosing_these_specific_movies",
            "movie_ids"
          ]
        }
        """;

        /*
         * Parse Ollama response
         * */
        InputStream responseStream = OllamaClient.generate(prompt, format);
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
        }

        return recommendedMovies.stream()
                .map(m -> String.format("%s [%s]", m.title(), m.genre()))
                .collect(Collectors.joining(", "));
    }

    public static String getShowListings(String message)
            throws URISyntaxException, IOException {
        String prompt = PromptGenerator.generateMovieTitleFetchingPrompt(message);
        String format = """
        {
          "type": "object",
          "properties": {
            "reason_for_choosing_this_movie_title": {
              "type": "string"
            },
            "movie_id": {
              "type": "integer"
            }
          },
          "required": [
            "reason_for_choosing_this_movie_title",
            "movie_id"
          ]
        }
        """;

        /*
         * Parse Ollama response
         * */
        InputStream responseStream = OllamaClient.generate(prompt, format);
        String responseJson = mapper.readTree(responseStream).get("response").asString();

        JsonNode responseNode = mapper.readTree(responseJson);

        String movieName = movieDAO.getMovieTitle(responseNode.get("movie_id").asInt());
        if (movieName == null) {
            return "Movie not found in the database.";
        }

        List<Show> shows = showDAO.getShows(movieName);

        String result = shows.stream()
                .map(s -> String.format("%s at %s", s.screen().name(), formatDateTime(s.startTime())))
                .collect(Collectors.joining(", "));

        return result.isBlank() ? "No shows found for the mentioned movie." : result;
    }

    public static String bookShow(String username, String message)
            throws URISyntaxException, IOException {
        String prompt = PromptGenerator.generateBookingDetailsFetchingPrompt(message);
        String format = """
        {
          "type": "object",
          "properties": {
            "movie_id": {
              "type": "integer"
            },
            "date": {
              "type": "string"
            },
            "time": {
              "type": "string"
            },
            "seats": {
              "type": "integer"
            }
          },
          "required": [
            "movie_id",
            "date",
            "time",
            "seats"
          ]
        }
        """;

        /*
         * Parse Ollama response
         * */
        InputStream responseStream = OllamaClient.generate(prompt, format);
        String responseJson = mapper.readTree(responseStream).get("response").asString();

        JsonNode responseNode = mapper.readTree(responseJson);

        String movieName = movieDAO.getMovieTitle(responseNode.get("movie_id").asInt());
        if (movieName == null) {
            return "Movie not found in the database.";
        }

        String dateStr = responseNode.get("date").asString();
        String timeStr = responseNode.get("time").asString() + ":00";
        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr));
        } catch (DateTimeParseException e) {
            return "Invalid date or time format %s %s".formatted(dateStr, timeStr);
        }

        int seatQty = responseNode.get("seats").asInt();
        if (seatQty <= 0) {
            return "Invalid number of seats.";
        }

        Show show = showDAO.getShow(movieName, dateTime);
        if (show == null) {
            return "Show not found for the mentioned movie and time. %s %s".formatted(movieName, formatDateTime(dateTime));
        }

        bookingDAO.addBooking(userDAO.getUser(username).id(), show.screen().id(), show.id(), seatQty);

        return "Booking successful for %d seats to '%s' at %s"
                .formatted(seatQty, movieName, formatDateTime(dateTime));
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        String date = dateTime.toLocalDate().toString();
        LocalTime time = dateTime.toLocalTime();
        String ftime = time.toString().substring(0, 5);

        return "%s %s".formatted(date, ftime);
    }
}
