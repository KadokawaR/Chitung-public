package lielietea.mirai.plugin.core.game.montecarlo.blackjack.data;

import lielietea.mirai.plugin.core.game.montecarlo.blackjack.enums.BlackJackPhase;

import java.util.ArrayList;
import java.util.List;

public class BlackJackData {

    long ID; // 如果是好友消息则为好友ID
    List<BlackJackPlayer> blackJackPlayerList;
    BlackJackPhase phase;
    int cardnumber;
    List<Integer> cardPile;

    public long getID() {
        return ID;
    }

    public List<BlackJackPlayer> getBlackJackPlayerList() {
        return blackJackPlayerList;
    }

    public BlackJackPhase getPhase() {
        return phase;
    }

    public int getCardnumber() {
        return cardnumber;
    }

    //有人触发，进入Callin阶段
    public BlackJackData(long id){
        ID = id;
        blackJackPlayerList = new ArrayList<>();
        phase = BlackJackPhase.Callin;
        cardnumber=0;
        cardPile = new ArrayList<>();
    }

    public void setPhase(BlackJackPhase ph) {
        phase = ph;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void addBlackJackPlayerList(BlackJackPlayer e) {
        blackJackPlayerList.add(e);
    }

    public void setBlackJackPlayerList(List<BlackJackPlayer> blackJackPlayerList) {
        this.blackJackPlayerList = blackJackPlayerList;
    }

    public void setCardnumber(int cardnumber) {
        this.cardnumber = cardnumber;
    }


    public List<Integer> getCardPile() {
        return cardPile;
    }

    public void setCardPile(List<Integer> cardPile) {
        this.cardPile = cardPile;
    }
}
