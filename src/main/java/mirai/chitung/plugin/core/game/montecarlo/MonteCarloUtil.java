package mirai.chitung.plugin.core.game.montecarlo;

import com.google.common.collect.ImmutableSet;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.MineUserData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.List;
import java.util.Set;

public interface MonteCarloUtil<GameUserData>{

    Set<String> functionKeyWords = ImmutableSet.of();

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
