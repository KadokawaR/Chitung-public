package chitung.plugin.core.game;

import chitung.plugin.administration.config.ConfigHandler;
import chitung.plugin.core.bank.SenoritaCounter;
import chitung.plugin.core.game.fish.Fishing;
import chitung.plugin.core.game.mahjongriddle.MahjongRiddle;
import chitung.plugin.core.game.montecarlo.CasinoCroupier;
import chitung.plugin.core.groupconfig.GroupConfigManager;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

public class GameCenter {

    public static void handle(MessageEvent event){

        if(event instanceof GroupMessageEvent){
            if(GroupConfigManager.gameConfig((GroupMessageEvent) event) && ConfigHandler.getINSTANCE().config.getGroupFC().isGame()) {
                if(GroupConfigManager.fishConfig((GroupMessageEvent) event)&&ConfigHandler.getINSTANCE().config.getGroupFC().isFish())Fishing.go(event);
                MahjongRiddle.riddleStart((GroupMessageEvent) event);
                if(GroupConfigManager.casinoConfig((GroupMessageEvent) event)&&ConfigHandler.getINSTANCE().config.getGroupFC().isCasino()){
                    CasinoCroupier.handle(event);
                    SenoritaCounter.go(event);
                }
            }
        }

        if(event instanceof FriendMessageEvent){
            if(ConfigHandler.getINSTANCE().config.getGroupFC().isGame()) {
                if(ConfigHandler.getINSTANCE().config.getGroupFC().isFish()) Fishing.go(event);
                if(ConfigHandler.getINSTANCE().config.getGroupFC().isCasino()){
                    SenoritaCounter.go(event);
                    CasinoCroupier.handle(event);
                }
            }
        }
    }

}
