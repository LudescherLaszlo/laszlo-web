package com.mrwhite.web;

import com.mrwhite.dto.*;
import com.mrwhite.game.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameController {
    private final GameService game;
    public GameController(GameService game) { this.game=game; }
    public record Join(@NotBlank String name) {}
    @PostMapping("/join")
    public ResponseEntity<?> join(@Valid @RequestBody Join request) {
        try { String[] result=game.join(request.name()); return ResponseEntity.ok(Map.of("sessionId",result[0],"playerId",result[1])); }
        catch(IllegalStateException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",e.getMessage()));}
        catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}
    }
}
