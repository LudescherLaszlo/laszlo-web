package com.mrwhite.game;
import java.util.List;
public record VoteResolution(Kind kind, String winnerId, List<String> tiedIds) {
    public enum Kind { EXECUTION, TIEBREAK, WHEEL }
}
