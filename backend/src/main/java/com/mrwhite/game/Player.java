package com.mrwhite.game;

import java.time.Instant;
import java.util.UUID;

public final class Player {
    private final String id = UUID.randomUUID().toString();
    private final String sessionId;
    private final String name;
    private PlayerStatus status;
    private Role role;
    private boolean connected;
    private Instant disconnectedSince;
    private boolean assignmentReady;
    private boolean executionReady;
    private boolean guessUsed;
    private String word;

    public Player(String sessionId, String name, PlayerStatus status) {
        this.sessionId = sessionId; this.name = name; this.status = status;
    }
    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getName() { return name; }
    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; if (connected) disconnectedSince = null; }
    public Instant getDisconnectedSince() { return disconnectedSince; }
    public void setDisconnectedSince(Instant value) { disconnectedSince = value; }
    public boolean isAssignmentReady() { return assignmentReady; }
    public void setAssignmentReady(boolean value) { assignmentReady = value; }
    public boolean isExecutionReady() { return executionReady; }
    public void setExecutionReady(boolean value) { executionReady = value; }
    public boolean isGuessUsed() { return guessUsed; }
    public void setGuessUsed(boolean value) { guessUsed = value; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
}
