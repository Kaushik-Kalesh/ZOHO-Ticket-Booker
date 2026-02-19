package org.example.dao;

import org.example.model.Movie;
import org.example.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    public void addMovie(String title, String director, String lead, String genre, LocalDate releaseDate) {
        String sql = "INSERT INTO movies (title, director, lead, genre, release_date) VALUES (?, ?, ?, ?, ?)";
        try (var conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, director);
            ps.setString(3, lead);
            ps.setString(4, genre);
            ps.setDate(5, java.sql.Date.valueOf(releaseDate));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Movie> getAllMovies() {
        String sql = "SELECT * FROM movies";
        List<Movie> movies = new ArrayList<>();
        try (var conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movies.add(new Movie(
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
        }
        return movies;
    }

    public String getMovieTitle(int id) {
        String sql = "SELECT title FROM movies WHERE id = ?";
        try (var conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
