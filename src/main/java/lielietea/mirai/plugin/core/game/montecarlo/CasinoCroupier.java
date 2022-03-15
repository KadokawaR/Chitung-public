package lielietea.mirai.plugin.core.game.montecarlo;

import lielietea.mirai.plugin.core.game.montecarlo.blackjack.BlackJack;
import lielietea.mirai.plugin.core.game.montecarlo.roulette.Roulette;
import net.mamoe.mirai.event.events.MessageEvent;

public class CasinoCroupier {

    private static final CasinoCroupier INSTANCE;

    static {
        INSTANCE = new CasinoCroupier();
    }

    public static CasinoCroupier getINSTANCE() {
        return INSTANCE;
    }

    public static void handle(MessageEvent event){
        switch(croupierStatus(event)){
            case 0:
                BlackJack.go(event);
                Roulette.go(event);
                return;
            case 1:
                BlackJack.go(event);
                return;
            case 2:
                Roulette.go(event);
                return;
        }

    }

    public static int croupierStatus (MessageEvent event){
        if(BlackJack.isInGamingProcess(event)) return 1;
        if(Roulette.isInGamingProcess(event)) return 2;
        return 0;
    }
}
