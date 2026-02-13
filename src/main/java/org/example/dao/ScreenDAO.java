package org.example.dao;

import org.example.model.Screen;
import org.example.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ScreenDAO {
    public int getScreenId(String name) {
        String sql = "SELECT id FROM screens WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Screen getScreen(int id) {
        String sql = "SELECT * FROM screens WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Screen(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getInt("capacity"), rs.getInt("price")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addScreen(String name, int capacity) {
        String sql = "INSERT INTO screens(name, capacity) VALUES(?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, capacity);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteScreen(int screenId) {
        String sql = "DELETE FROM screens WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, screenId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Screen> fetchScreens() {
        String sql = "SELECT * FROM screens";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            List<Screen> screens = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Screen screen = new Screen(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getInt("capacity"), rs.getInt("price")
                );
                screens.add(screen);
            }

            return screens;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
