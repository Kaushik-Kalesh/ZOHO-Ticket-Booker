package org.example.model;

import java.time.LocalDateTime;

public record Booking(
        int id,
        User user,
        Show show,
        int seatsBooked,
        LocalDateTime bookingTime
) {}
