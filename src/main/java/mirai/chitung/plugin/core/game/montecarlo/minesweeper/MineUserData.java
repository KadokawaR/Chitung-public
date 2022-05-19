package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSaiData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;

public class MineUserData {
    Contact sender;
    Contact subject;
    int bet;
    List<MineData> betList;


    public MineUserData(MessageEvent event, int bet) {
        this.sender = event.getSender();
        this.subject = event.getSubject();
        this.bet = bet;
        this.betList = new ArrayList<>();
    }

    public void addBetAmount(int additionalBet){
        this.bet+=additionalBet;
    }

    public void addBet(MineData data){
        this.betList.add(data);
    }

    public void addBet(List<MineData> data){

        this.betList.addAll(data);
    }

    public boolean isFriend(){
        return this.subject.getId()==this.sender.getId();
    }

    public boolean isGroup(){
        return this.subject.getId()!=this.sender.getId();
    }
}
