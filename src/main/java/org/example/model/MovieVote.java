package org.example.model;

public record MovieVote(
        int id,
        Movie movie,
        User user
) {}
