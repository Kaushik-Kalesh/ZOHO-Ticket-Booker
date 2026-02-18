package org.example.dao;

import org.example.model.Movie;
import org.example.util.DBUtil;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MovieVoteDAO {
    public void addVote(int movieId, int userId) {
        String sql = "INSERT INTO movie_votes (movie_id, user_id) VALUES (?, ?)";

        try(var conn = DBUtil.getConnection();
            var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movieId);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Movie> getMoviesLikedByUser(int userId) {
        List<Movie> results = new ArrayList<>();

        String sql = """
        SELECT m.*
        FROM movies m
        JOIN movie_votes mv
            ON m.id = mv.movie_id
        WHERE mv.user_id = ?
        """;

        try(var conn = DBUtil.getConnection();
            var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("director"),
                        rs.getString("lead"),
                        rs.getString("genre"),
                        rs.getDate("release_date").toLocalDate()
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return results;
    }

    public List<Movie> getMoviesLikedBySimilarUsers(int userId) {

        List<Movie> results = new ArrayList<>();

        String sql = """
        WITH candidate_movies AS (
            SELECT mv.movie_id,
                   COUNT(*) AS score
            FROM movie_votes mv
            WHERE mv.user_id IN (
                SELECT mv2.user_id
                FROM movie_votes mv1
                JOIN movie_votes mv2
                  ON mv1.movie_id = mv2.movie_id
                WHERE mv1.user_id = ?
                  AND mv2.user_id != ?
                GROUP BY mv2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 20
            )
            AND mv.movie_id NOT IN (
                SELECT movie_id
                FROM movie_votes
                WHERE user_id = ?
            )
            GROUP BY mv.movie_id
            ORDER BY score DESC
            LIMIT 20
        )
        SELECT m.*
        FROM candidate_movies c
        JOIN movies m ON m.id = c.movie_id
        ORDER BY c.score DESC
        """;

        try (var conn = DBUtil.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Movie(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("director"),
                            rs.getString("lead"),
                            rs.getString("genre"),
                            rs.getDate("release_date").toLocalDate()
                    ));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return results;
    }
}
