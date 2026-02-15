package org.example.dao;

import org.example.model.Booking;
import org.example.model.Screen;
import org.example.model.Show;
import org.example.model.User;
import org.example.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    public int getAvailableSeats(int screenId, int showId) {
        String sql1 = "SELECT capacity FROM screens WHERE id = ?";
        String sql2 = "SELECT seat_qty FROM bookings WHERE screen_id = ? AND show_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sql1);
             PreparedStatement ps2= conn.prepareStatement(sql2)) {

            ps1.setInt(1, screenId);
            ResultSet rs1 = ps1.executeQuery();
            int capacity = 0;
            if (rs1.next()) {
                capacity = rs1.getInt("capacity");
            }

            ps2.setInt(1, screenId);
            ps2.setInt(2, showId);
            ResultSet rs = ps2.executeQuery();
            int bookedSeats = 0;
            while (rs.next()) {
                bookedSeats += rs.getInt("seat_qty");
            }
            return capacity - bookedSeats;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void addBooking(int userId, int screenId, int showId, int seats, int cost) {
        String sql1 = "INSERT INTO bookings (user_id, screen_id, show_id, seat_qty) VALUES (?, ?, ?, ?)";
        String sql2 = "UPDATE users SET wallet_bal = wallet_bal - ?, loyalty_pts = loyalty_pts + ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sql1);
             PreparedStatement ps2 = conn.prepareStatement(sql2)) {
            ps1.setInt(1, userId);
            ps1.setInt(2, screenId);
            ps1.setInt(3, showId);
            ps1.setInt(4, seats);
            ps1.executeUpdate();

            ps2.setInt(1, cost);
            ps2.setInt(2, 10);
            ps2.setInt(3, userId);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Booking> getAllBookings(String username) {

        String sql = """
        SELECT
            b.id,
            b.seat_qty,
            b.user_id,
            b.screen_id,
            b.show_id,
            b.booking_time,

            u.username,
            u.password,
            u.wallet_bal,
            u.loyalty_pts,
        
            s.name,
            s.price,
            s.capacity,

            sh.title,
            sh.start_time
        
        FROM bookings b
        JOIN screens s ON b.screen_id = s.id
        JOIN shows sh  ON b.show_id = sh.id
        JOIN users u on b.user_id = u.id
        WHERE u.username = ?
        """;

        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("wallet_bal"),
                        rs.getInt("loyalty_pts")
                );

                Screen screen = new Screen(
                        rs.getInt("screen_id"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        rs.getInt("capacity")
                );

                Show show = new Show(
                        rs.getInt("show_id"),
                        rs.getString("title"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        screen
                );

                Booking booking = new Booking(
                        rs.getInt("id"),
                        user,
                        show,
                        rs.getInt("seat_qty"),
                        rs.getTimestamp("booking_time").toLocalDateTime()
                );

                bookings.add(booking);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }

}
