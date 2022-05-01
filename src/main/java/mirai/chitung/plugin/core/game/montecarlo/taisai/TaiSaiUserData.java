package mirai.chitung.plugin.core.game.montecarlo.taisai;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;

public class TaiSaiUserData {

    Contact subject;
    Contact sender;
    int bet;
    List<TaiSaiData> betList;

    TaiSaiUserData(MessageEvent event,int bet){
        this.subject=event.getSubject();
        this.sender=event.getSender();
        this.bet=bet;
        this.betList = new ArrayList<>();
    }

    public void addBetAmount(int additionalBet){
        this.bet+=additionalBet;
    }

    public void addBet(TaiSaiData tsd){
        this.betList.add(tsd);
    }

    public void addBet(List<TaiSaiData> tsd){
        this.betList.addAll(tsd);
    }

    public boolean isFriend(){
        return this.subject.getId()==this.sender.getId();
    }

    public boolean isGroup(){
        return this.subject.getId()!=this.sender.getId();
    }

}
