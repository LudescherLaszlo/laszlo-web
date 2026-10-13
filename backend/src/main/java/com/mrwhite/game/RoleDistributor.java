package com.mrwhite.game;

public final class RoleDistributor {
    private RoleDistributor() {}
    public static RoleDistribution distribution(int players) {
        if (players < 3 || players > 30) throw new IllegalArgumentException("Players must be between 3 and 30");
        if (players == 3) return new RoleDistribution(0, 1);
        int undercovers = (players - 1) / 6 + 1;
        int whites = (players + 2) / 6;
        return new RoleDistribution(undercovers, whites);
    }
}
