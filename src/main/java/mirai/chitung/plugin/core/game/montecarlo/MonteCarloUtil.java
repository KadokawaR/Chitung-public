package mirai.chitung.plugin.core.game.montecarlo;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.List;

public interface MonteCarloUtil<GameUserData>{

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean senderIsInGamingProcess(MessageEvent event);

    boolean subjectIsInGamingProcess(Contact subject);

    boolean hasStarted(Contact subject);

    int getBet(Contact sender,Contact subject);

    void addBet(Contact sender,int bet);

    GameUserData getData(Contact sender);

    void deleteAllSubject(Contact subject);

    List<GameUserData> getUserList(Contact subject);

    void clear(Contact subject);

}
