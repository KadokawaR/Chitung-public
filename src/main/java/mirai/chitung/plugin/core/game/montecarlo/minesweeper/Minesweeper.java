package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import mirai.chitung.plugin.core.game.montecarlo.MonteCarloGame;
import net.mamoe.mirai.event.events.MessageEvent;

public class Minesweeper implements MonteCarloGame<MessageEvent> {

    @Override
    public void handle(MessageEvent event) {

    }

    @Override
    public void process(MessageEvent event, String message) {

    }

    @Override
    public void start(MessageEvent event, String message) {

    }

    @Override
    public void function(MessageEvent event, String message) {

    }

    @Override
    public void bet(MessageEvent event, String message) {

    }

    @Override
    public boolean matchStart(String message) {
        return false;
    }

    @Override
    public boolean matchBet(String message) {
        return false;
    }

    @Override
    public boolean matchFunction(String message) {
        return false;
    }

    @Override
    public boolean matchGame(String message) {
        return false;
    }

    @Override
    public boolean isInGamingProcess(MessageEvent event) {
        return false;
    }
}
