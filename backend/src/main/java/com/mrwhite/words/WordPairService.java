package com.mrwhite.words;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WordPairService {
    private final WordPairRepository repository;
    private final Deque<Long> recent = new ArrayDeque<>();
    private final Random random = new Random();
    public WordPairService(WordPairRepository repository) { this.repository = repository; }
    public synchronized WordPair randomPair(int excludedCount) {
        List<WordPair> all = repository.findAll();
        if (all.isEmpty()) throw new IllegalStateException("No word pairs are configured");
        List<WordPair> choices = all.stream().filter(p -> !recent.contains(p.getId())).toList();
        if (choices.isEmpty()) choices = all;
        WordPair chosen = choices.get(random.nextInt(choices.size()));
        recent.remove(chosen.getId()); recent.addLast(chosen.getId());
        while (recent.size() > excludedCount) recent.removeFirst();
        return chosen;
    }
    public String[] randomAssignment(WordPair pair) {
        return random.nextBoolean() ? new String[] { pair.getWordA(), pair.getWordB() }
                : new String[] { pair.getWordB(), pair.getWordA() };
    }
}
