package com.mrwhite.game;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

class WinEvaluatorTest {
    private Player p(Role r, PlayerStatus s){Player p=new Player(UUID.randomUUID().toString(),r.name(),s);p.setRole(r);return p;}
    @Test void evaluatesConditionsInOrder(){
        assertThat(WinEvaluator.evaluate(List.of(p(Role.MR_WHITE,PlayerStatus.ALIVE)),true).type()).isEqualTo(WinType.MR_WHITE);
        assertThat(WinEvaluator.evaluate(List.of(p(Role.CIVILIAN,PlayerStatus.ALIVE)),false).type()).isEqualTo(WinType.CIVILIANS);
        assertThat(WinEvaluator.evaluate(List.of(p(Role.UNDERCOVER,PlayerStatus.ALIVE),p(Role.CIVILIAN,PlayerStatus.ALIVE)),false).type()).isEqualTo(WinType.UNDERCOVERS);
        assertThat(WinEvaluator.evaluate(List.of(p(Role.MR_WHITE,PlayerStatus.ALIVE),p(Role.CIVILIAN,PlayerStatus.ALIVE)),false).type()).isEqualTo(WinType.MR_WHITE);
    }
}
