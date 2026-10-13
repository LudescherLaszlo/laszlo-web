package com.mrwhite.game;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoteResolverTest {
    @Test void resolvesSingleTieAndWheel() {
        assertThat(VoteResolver.resolve(Map.of("a","x","b","x","c","y"),false).kind()).isEqualTo(VoteResolution.Kind.EXECUTION);
        assertThat(VoteResolver.resolve(Map.of("a","x","b","y"),false).kind()).isEqualTo(VoteResolution.Kind.TIEBREAK);
        assertThat(VoteResolver.resolve(Map.of("a","x","b","y"),true).kind()).isEqualTo(VoteResolution.Kind.WHEEL);
    }
}
