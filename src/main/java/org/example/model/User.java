package org.example.model;

public record User(
        int id,
        String username,
        String password,
        int walletBal,
        int loyaltyPts
) {}
