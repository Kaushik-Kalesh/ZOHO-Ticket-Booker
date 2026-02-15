package org.example.dao;

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

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setObject(2, startTime);
            ps.setInt(3, screenId);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean deleteShow(int showId) {
        String sql = "DELETE FROM shows WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, showId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Show> getAllShows() {

        String sql = """
            SELECT sh.id,
                   sh.title,
                   sh.start_time,
                   sh.screen_id
            FROM shows sh
            JOIN screens sc ON sh.screen_id = sc.id
        """;

        List<Show> shows = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Show show = new Show(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        screenDAO.getScreen(rs.getInt("screen_id"))
                );
                shows.add(show);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return shows;
    }
}
