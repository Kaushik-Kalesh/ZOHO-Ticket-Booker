package org.example.model;

import java.time.LocalDate;

public record Movie(
        int id,
        String title,
        String director,
        String lead,
        String genre,
        LocalDate releaseDate
) {}
