package mirai.chitung.plugin.core.game;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.bank.SenoritaCounter;
import mirai.chitung.plugin.core.game.fish.Fishing;
import mirai.chitung.plugin.core.game.mahjongriddle.MahjongRiddle;
import mirai.chitung.plugin.core.game.montecarlo.CasinoCroupier;
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

public class GameCenter {

    public static void handle(MessageEvent event){

        if(event instanceof GroupMessageEvent){
            if(GroupConfigManager.gameConfig((GroupMessageEvent) event) && ConfigHandler.getINSTANCE().config.getGroupFC().isGame()) {
                MahjongRiddle.riddleStart((GroupMessageEvent) event);
                if(GroupConfigManager.casinoConfig((GroupMessageEvent) event)&&ConfigHandler.getINSTANCE().config.getGroupFC().isCasino()){
                    CasinoCroupier.handle(event);
                    SenoritaCounter.go(event);
                }
            }
        }

        if(event instanceof FriendMessageEvent){
            if(ConfigHandler.getINSTANCE().config.getFriendFC().isGame()) {
                if(ConfigHandler.getINSTANCE().config.getFriendFC().isCasino()){
                    SenoritaCounter.go(event);
                    CasinoCroupier.handle(event);
                }
            }
        }
    }

}
