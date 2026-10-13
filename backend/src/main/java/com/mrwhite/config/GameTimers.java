package com.mrwhite.config;

import com.mrwhite.game.GameService;
import org.springframework.scheduling.annotation.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableScheduling
public class GameTimers {
    private final GameService game;
    public GameTimers(GameService game){this.game=game;}
    @Scheduled(fixedDelay = 60_000)
    public void inactivity(){game.resetForInactivity();}
}
