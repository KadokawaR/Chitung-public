package mirai.chitung.plugin.core.game.montecarlo;

import net.mamoe.mirai.event.events.MessageEvent;

public interface MonteCarloGame<T extends MessageEvent> {

    void handle(T event);

    void process(T event, String message);

    void start(T event,String message);

    void function(T event,String message);

    void bet(T event,String message);

    boolean matchStart(String message);

    boolean matchBet(String message);

    boolean matchFunction(String message);

    boolean matchGame(String message);

    boolean isInGamingProcess(T event);

}
