package mirai.chitung.plugin.core.game.montecarlo;

import mirai.chitung.plugin.core.game.montecarlo.blackjack.BlackJack;
import mirai.chitung.plugin.core.game.montecarlo.roulette.Roulette;
import mirai.chitung.plugin.core.harbor.Harbor;
import net.mamoe.mirai.event.events.MessageEvent;

public class CasinoCroupier {

    public static void handle(MessageEvent event){
        switch(croupierStatus(event)){
            case 0:
                if(Harbor.isReachingPortLimit(event)) return;
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

    //重置blackjack
    static void flush(MessageEvent event,String message){
        if(!message.equalsIgnoreCase("/flush")) return;
        BlackJack.cancelMark(event);
        Roulette.cancelMark(event);
        event.getSubject().sendMessage("已经重置娱乐游戏。");
    }

    public static int croupierStatus (MessageEvent event){
        if(BlackJack.isInGamingProcess(event)) return 1;
        if(Roulette.isInGamingProcess(event)) return 2;
        return 0;
    }
}
