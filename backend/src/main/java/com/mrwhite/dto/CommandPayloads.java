package com.mrwhite.dto;
public final class CommandPayloads {
    private CommandPayloads() {}
    public record Start(int expectedCount) {}
    public record Vote(String targetId) {}
    public record Verdict(boolean correct) {}
    public record Remove(String playerId) {}
}
