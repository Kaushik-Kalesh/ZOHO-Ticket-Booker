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

    public void addBooking(int userId, int screenId, int showId, int seats) {

        String lockScreenSql = """
        SELECT id FROM screens
        WHERE id = ?
        FOR UPDATE
        """;

        String availabilitySql = """
        SELECT s.capacity - COALESCE(SUM(b.seat_qty), 0) AS available, s.price
        FROM screens s
        LEFT JOIN bookings b
            ON s.id = b.screen_id AND b.show_id = ?
        WHERE s.id = ?
        GROUP BY s.capacity, s.price
        """;

        String walletSql = """
        SELECT wallet_bal FROM users
        WHERE id = ?
        FOR UPDATE
        """;

        String insertSql = """
        INSERT INTO bookings (user_id, screen_id, show_id, seat_qty)
        VALUES (?, ?, ?, ?)
        """;

        String updateUserSql = """
        UPDATE users
        SET wallet_bal = wallet_bal - ?,
            loyalty_pts = loyalty_pts + ?
        WHERE id = ?
        """;

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            int price;
            int available;

            try (PreparedStatement ps = conn.prepareStatement(lockScreenSql)) {
                ps.setInt(1, screenId);
                ps.executeQuery();
            }

            try (PreparedStatement ps = conn.prepareStatement(availabilitySql)) {
                ps.setInt(1, showId);
                ps.setInt(2, screenId);

                ResultSet rs = ps.executeQuery();

                if (!rs.next())
                    throw new RuntimeException("Screen not found");

                available = rs.getInt("available");
                price = rs.getInt("price");

                if (available < seats)
                    throw new RuntimeException("Not enough seats available");
            }

            int totalCost = price * seats;

            try (PreparedStatement ps = conn.prepareStatement(walletSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next())
                    throw new RuntimeException("User not found");

                if (rs.getInt("wallet_bal") < totalCost)
                    throw new RuntimeException("Insufficient wallet balance");
            }

            try (PreparedStatement ps = conn.prepareStatement(updateUserSql)) {
                ps.setInt(1, totalCost);
                ps.setInt(2, LOYALTY_POINTS_PER_BOOKING);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, screenId);
                ps.setInt(3, showId);
                ps.setInt(4, seats);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void cancelBooking(int bookingId) {

        String selectSql = """
        SELECT b.user_id, b.seat_qty, s.price
        FROM bookings b
        JOIN screens s ON b.screen_id = s.id
        WHERE b.id = ?
        FOR UPDATE
        """;

        String deleteSql = "DELETE FROM bookings WHERE id = ?";
        String refundSql = """
        UPDATE users
        SET wallet_bal = wallet_bal + ?,
            loyalty_pts = loyalty_pts - ?
        WHERE id = ?
        """;

        try (Connection conn = DBUtil.getConnection()) {

            conn.setAutoCommit(false);

            int userId;
            int refund;

            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, bookingId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next())
                    throw new RuntimeException("Booking not found");

                userId = rs.getInt("user_id");
                refund = rs.getInt("seat_qty") * rs.getInt("price");
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(refundSql)) {
                ps.setInt(1, refund);
                ps.setInt(2, LOYALTY_POINTS_PER_BOOKING);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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
