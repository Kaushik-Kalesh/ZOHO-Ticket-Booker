package org.example.dao;

import org.example.model.Screen;
import org.example.model.Show;
import org.example.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShowDAO {
    private final ScreenDAO screenDAO = new ScreenDAO();

    public void addShow(String title, LocalDateTime startTime, int screenId) {
        String sql = "INSERT INTO shows(title, start_time, screen_id) VALUES(?, ?, ?)";

        try (var conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setObject(2, startTime);
            ps.setInt(3, screenId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<Show> getShows(String title) {
        String sql = """
        SELECT sh.id,
               sh.title,
               sh.start_time,
               sc.id AS screen_id,
               sc.name,
               sc.capacity,
               sc.price
        FROM shows sh
        JOIN screens sc ON sh.screen_id = sc.id
        """ + (title.isBlank() ? "" : " WHERE sh.title = ?");

        List<Show> shows = new ArrayList<>();

        try (var conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!title.isBlank()) {
                ps.setString(1, title);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Screen screen = new Screen(
                        rs.getInt("screen_id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getInt("price")
                );

                Show show = new Show(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        screen
                );

                shows.add(show);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return shows;
    }
}
