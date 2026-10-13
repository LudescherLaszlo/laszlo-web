package com.mrwhite.dto;

import com.mrwhite.game.*;
import java.util.*;

public record PublicState(long stateVersion, long serverTime, Phase phase, int round, int minPlayers, int maxPlayers,
                          List<PlayerView> players, List<String> speakingOrder, String currentSpeakerId,
                          VoteView vote, ReadyView ready, ExecutionView execution, WheelView wheel,
                          RevealView reveal, GuessView guess, VictoryView victory, Notice notice) {
    public record PlayerView(String id, String name, PlayerStatus status, boolean connected, Long disconnectedSince, Role role) {}
    public record VoteView(int voteRound, List<String> candidateIds, int votedCount, int totalVoters) {}
    public record ReadyView(int readyCount, int totalRequired) {}
    public record ExecutionView(String executedPlayerId, Map<String,Integer> voteCounts, String resolvedBy) {}
    public record WheelView(List<String> candidateIds, String winnerId, long startAt, long durationMs) {}
    public record RevealView(String playerId, Role role, long endsAt, String reason) {}
    public record GuessView(String playerId, int judgesAnswered, int judgesTotal) {}
    public record VictoryView(WinType winnerType, String abortedBy, String civilianWord, String undercoverWord, List<VictoryPlayer> players) {}
    public record VictoryPlayer(String id, String name, Role originalRole, boolean survived) {}
    public record Notice(long id, String text) {}
}
