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

    private final int LOYALTY_POINTS_PER_BOOKING = 10;

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
            ps2.setInt(2, LOYALTY_POINTS_PER_BOOKING);
            ps2.setInt(3, userId);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelBooking(int id) {
        String sql1 = """
        SELECT 
            b.user_id, 
            b.seat_qty, 
            s.price 
        FROM bookings b
        JOIN screens s
        ON b.screen_id = s.id
        WHERE b.id = ?
        """;
        String sql2 = "DELETE FROM bookings WHERE id = ?";
        String sql3 = "UPDATE users SET wallet_bal = wallet_bal + ?, loyalty_pts = loyalty_pts - ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps1 = conn.prepareStatement(sql1);
             PreparedStatement ps2 = conn.prepareStatement(sql2);
             PreparedStatement ps3 = conn.prepareStatement(sql3)) {

            ps1.setInt(1, id);
            ResultSet rs = ps1.executeQuery();
            int userId = 0;
            int cost = 0;
            if (rs.next()) {
                userId = rs.getInt("user_id");
                cost = rs.getInt("seat_qty") * rs.getInt("price");
            }

            ps2.setInt(1, id);
            ps2.executeUpdate();

            ps3.setInt(1, cost);
            ps3.setInt(2, LOYALTY_POINTS_PER_BOOKING);
            ps3.setInt(3, userId);
            ps3.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markReminderSent(int bookingId) {
        String sql = "UPDATE bookings SET reminder_sent = TRUE WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.executeUpdate();
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
            u.email,
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
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("wallet_bal"),
                        rs.getInt("loyalty_pts")
                );

                Screen screen = new Screen(
                        rs.getInt("screen_id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getInt("price")
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

    public List<Booking> getUpcomingBookings() {
        String sql = """
        SELECT
            b.id,
            b.seat_qty,
            b.user_id,
            b.screen_id,
            b.show_id,
            b.booking_time,

            u.username,
            u.email,
            u.password,
            u.wallet_bal,
            u.loyalty_pts,
        
            s.name,
            s.price,
            s.capacity,

            sh.title,
            sh.start_time
        FROM bookings b
        JOIN users u ON b.user_id = u.id
        JOIN screens s ON b.screen_id = s.id
        JOIN shows sh ON b.show_id = sh.id
        WHERE sh.start_time BETWEEN NOW() + INTERVAL '59 minutes'
                                AND NOW() + INTERVAL '61 minutes'
        AND b.reminder_sent = FALSE
        """;

        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getInt("wallet_bal"),
                        rs.getInt("loyalty_pts")
                );

                Screen screen = new Screen(
                        rs.getInt("screen_id"),
                        rs.getString("name"),
                        rs.getInt("capacity"),
                        rs.getInt("price")
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
