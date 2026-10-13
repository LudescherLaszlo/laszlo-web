package com.mrwhite.game;

import java.util.*;

public final class VoteResolver {
    private VoteResolver() {}
    public static VoteResolution resolve(Map<String, String> votes, boolean tieBreak) {
        Map<String, Integer> counts = new HashMap<>();
        votes.values().forEach(id -> counts.merge(id, 1, Integer::sum));
        if (counts.isEmpty()) return new VoteResolution(VoteResolution.Kind.EXECUTION, null, List.of());
        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        List<String> tied = counts.entrySet().stream().filter(e -> e.getValue() == max)
                .map(Map.Entry::getKey).sorted().toList();
        if (tied.size() == 1) return new VoteResolution(VoteResolution.Kind.EXECUTION, tied.get(0), tied);
        return new VoteResolution(tieBreak ? VoteResolution.Kind.WHEEL : VoteResolution.Kind.TIEBREAK, null, tied);
    }
}
