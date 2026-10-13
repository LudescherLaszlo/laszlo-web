package com.mrwhite.web;

import com.mrwhite.dto.CommandPayloads;
import com.mrwhite.game.GameService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {
    private final GameService game;
    public StompController(GameService game){this.game=game;}
    private String session(SimpMessageHeaderAccessor h){return h.getUser()==null?null:h.getUser().getName();}
    @MessageMapping("/sync") public void sync(SimpMessageHeaderAccessor h){game.command(session(h),"sync",null);}
    @MessageMapping("/leave") public void leave(SimpMessageHeaderAccessor h){game.command(session(h),"leave",null);}
    @MessageMapping("/start") public void start(CommandPayloads.Start p,SimpMessageHeaderAccessor h){game.command(session(h),"start",p);}
    @MessageMapping("/assignment-ready") public void assignment(SimpMessageHeaderAccessor h){game.command(session(h),"assignment-ready",null);}
    @MessageMapping("/next-speaker") public void next(SimpMessageHeaderAccessor h){game.command(session(h),"next-speaker",null);}
    @MessageMapping("/skip-speaker") public void skip(SimpMessageHeaderAccessor h){game.command(session(h),"skip-speaker",null);}
    @MessageMapping("/end-round") public void endRound(SimpMessageHeaderAccessor h){game.command(session(h),"end-round",null);}
    @MessageMapping("/vote") public void vote(CommandPayloads.Vote p,SimpMessageHeaderAccessor h){game.command(session(h),"vote",p);}
    @MessageMapping("/ready") public void ready(SimpMessageHeaderAccessor h){game.command(session(h),"ready",null);}
    @MessageMapping("/declare-guess") public void guess(SimpMessageHeaderAccessor h){game.command(session(h),"declare-guess",null);}
    @MessageMapping("/guess-verdict") public void verdict(CommandPayloads.Verdict p,SimpMessageHeaderAccessor h){game.command(session(h),"guess-verdict",p);}
    @MessageMapping("/remove-player") public void remove(CommandPayloads.Remove p,SimpMessageHeaderAccessor h){game.command(session(h),"remove-player",p);}
    @MessageMapping("/end-game") public void abort(SimpMessageHeaderAccessor h){game.command(session(h),"end-game",null);}
    @MessageMapping("/return-to-lobby") public void lobby(SimpMessageHeaderAccessor h){game.command(session(h),"return-to-lobby",null);}
}
