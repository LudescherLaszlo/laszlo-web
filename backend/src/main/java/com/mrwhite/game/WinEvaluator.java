package com.mrwhite.game;

import java.util.Collection;

public final class WinEvaluator {
    private WinEvaluator() {}
    public static WinResult evaluate(Collection<Player> players, boolean correctGuess) {
        if (correctGuess) return new WinResult(WinType.MR_WHITE);
        int civilians = 0, undercovers = 0, whites = 0;
        for (Player p : players) if (p.getStatus() == PlayerStatus.ALIVE) {
            if (p.getRole() == Role.CIVILIAN) civilians++;
            else if (p.getRole() == Role.UNDERCOVER) undercovers++;
            else if (p.getRole() == Role.MR_WHITE) whites++;
        }
        if (undercovers == 0 && whites == 0) return new WinResult(WinType.CIVILIANS);
        if (undercovers >= 1 && undercovers >= civilians)
            return new WinResult(whites > 0 ? WinType.UNDERCOVERS_AND_MR_WHITE : WinType.UNDERCOVERS);
        if (undercovers == 0 && whites >= 1 && whites >= civilians) return new WinResult(WinType.MR_WHITE);
        return null;
    }
}
