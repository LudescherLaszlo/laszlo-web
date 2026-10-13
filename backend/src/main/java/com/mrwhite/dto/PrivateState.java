package com.mrwhite.dto;
import com.mrwhite.game.*;
import java.util.*;
public record PrivateState(Me me, Role role, String word, String civilianWord, String undercoverWord, String myVoteTargetId,
                           String executionMessage, Judge judge, List<String> removablePlayerIds, Permissions can) {
    public record Me(String id, String name, PlayerStatus status) {}
    public record Judge(String guessingPlayerId, String civilianWord, boolean answered) {}
    public record Permissions(boolean start, boolean assignmentReady, boolean done, boolean skipSpeaker, boolean endRound,
                              boolean vote, boolean ready, boolean declareGuess, boolean judge) {}
}
