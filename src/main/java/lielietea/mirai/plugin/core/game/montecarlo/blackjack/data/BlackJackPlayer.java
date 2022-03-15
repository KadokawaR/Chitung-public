package lielietea.mirai.plugin.core.game.montecarlo.blackjack.data;

import lielietea.mirai.plugin.utils.IdentityUtil;

import java.util.ArrayList;
import java.util.List;

public class BlackJackPlayer {
    long ID;
    List<Integer> cards;
    int bet;
    boolean betPair;
    boolean hasSplit;
    boolean canOperate;
    boolean isDouble;
    boolean hasAssurance;
    boolean hasBusted;
    boolean hasSurrendered;

    //生成普通玩家
    public BlackJackPlayer(long id, int betNumber){
        ID = id;
        cards = new ArrayList<>();
        bet = betNumber;
        betPair = false;
        hasSplit = false;
        canOperate = false;
        isDouble = false;
        hasAssurance = false;
        hasBusted = false;
        hasSurrendered = false;
    }

    //生成庄家
    public BlackJackPlayer(boolean bookmaker){
        if(bookmaker){ ID = 0; }
        cards = new ArrayList<>();
        bet = 0;
        betPair = false;
        hasSplit = false;
        canOperate = false;
        isDouble = false;
        hasAssurance = false;
        hasBusted = false;
        hasSurrendered = false;
    }

    public void addCards(Integer card){
        this.cards.add(card);
    }

    public boolean isBookmaker(){
        return this.ID==0;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public boolean isBetPair() {
        return betPair;
    }

    public void setBetPair(boolean betPair) {
        this.betPair = betPair;
    }

    public boolean isHasSplit() {
        return hasSplit;
    }

    public void setHasSplit(boolean hasSplit) {
        this.hasSplit = hasSplit;
    }

    public boolean isCanOperate() {
        return canOperate;
    }

    public void setCanOperate(boolean canOperate) {
        this.canOperate = canOperate;
    }

    public boolean isDouble() {
        return isDouble;
    }

    public void setDouble(boolean aDouble) {
        isDouble = aDouble;
    }

    public boolean isHasAssurance() {
        return hasAssurance;
    }

    public void setHasAssurance(boolean hasAssurance) {
        this.hasAssurance = hasAssurance;
    }

    public boolean isHasBusted() {
        return hasBusted;
    }

    public void setHasBusted(boolean hasBusted) {
        this.hasBusted = hasBusted;
    }

    public boolean isHasSurrendered() {
        return hasSurrendered;
    }

    public void setHasSurrendered(boolean hasSurrendered) {
        this.hasSurrendered = hasSurrendered;
    }
}
