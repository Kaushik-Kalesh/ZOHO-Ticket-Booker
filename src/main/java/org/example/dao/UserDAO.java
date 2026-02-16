package org.example.dao;

import org.example.model.User;
import org.example.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean validateUser(String username, String password) {

        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean createUser(String username, String email, String password) {
        String sql = "INSERT INTO users(username, email, password) VALUES(?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("wallet_bal"),
                        rs.getInt("loyalty_pts")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void loadWallet(String username, int amount) {
        String sql = "UPDATE users SET wallet_bal = wallet_bal + ? WHERE username=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, amount);
            ps.setString(2, username);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redeemLoyaltyPoints(String username) {
        String sql1 = "SELECT loyalty_pts FROM users WHERE username=?";
        String sql2 = "UPDATE users SET loyalty_pts = 0, wallet_bal = wallet_bal + ? WHERE username=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sql1);
             PreparedStatement ps2 = conn.prepareStatement(sql2)) {

            int points = 0;
            ps1.setString(1, username);
            ResultSet rs = ps1.executeQuery();
            if (rs.next()) {
                points = rs.getInt("loyalty_pts");
            }

            ps2.setInt(1, points);
            ps2.setString(2, username);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
