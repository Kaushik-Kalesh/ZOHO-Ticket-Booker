package org.example.model;

import java.time.LocalDateTime;

public record Show(
        int id,
        String title,
        LocalDateTime startTime,
        Screen screen
) {}
