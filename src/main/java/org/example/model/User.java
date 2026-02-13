package org.example.model;

public record User(
        String username,
        String password,
        int walletBal,
        int loyaltyPts
) {}
