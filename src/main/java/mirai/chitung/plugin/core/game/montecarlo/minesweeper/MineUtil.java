package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import com.google.common.collect.ImmutableSet;
import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSai;
import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSaiUserData;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MineUtil {

    static final String Rules = "里格斯公司邀请您参与本局扫雷，请在60秒之内输入 /bet+数字 参与游戏。";
    static final String Stops = "本局扫雷已经取消。";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。\n在这一阶段不会向您收取南瓜比索。由于扫雷可以多重下注，因此不建议设置过大的 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。如有多重下注，请使用空格隔开。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";

    static final int GapTime = 60;

    static Set<String> functionKeyWords = ImmutableSet.of(


    );

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean senderIsInGamingProcess(MessageEvent event){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.getId()==event.getSender().getId() && data.subject.getId()==event.getSubject().getId()) return true;
            if(hasStarted(event.getSubject())) return true;
        }
        return false;
    }

    static boolean subjectIsInGamingProcess(Contact subject){
        for (MineUserData data: Minesweeper.data){
            if(data.subject.getId()==subject.getId()) return true;
        }
        return false;
    }

    static boolean hasStarted(Contact subject){
        for(Contact c:Minesweeper.startBetList){
            if(c.getId()==subject.getId()) return true;
        }
        return false;
    }

    static int getBet(Contact sender,Contact subject){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.getId()==sender.getId()&&data.subject.getId()==subject.getId()) return data.bet;
        }
        return 0;
    }

    static void addBet(Contact sender,int bet){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.getId()==sender.getId()) data.addBetAmount(bet);
        }
    }

    static MineUserData getData(Contact sender){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.getId()==sender.getId()) return data;
        }
        return null;
    }

    static void deleteAllSubject(Contact subject){
        Minesweeper.data.removeIf(data -> data.subject.getId() == subject.getId());
    }

    static List<MineUserData> getUserList(Contact subject){
        return Minesweeper.data.stream().filter(userData -> userData.subject.getId()==subject.getId()).collect(Collectors.toList());
    }

    static void clear(Contact subject){
        deleteAllSubject(subject);
        Minesweeper.startBetList.remove(subject);
        Minesweeper.isInBetList.remove(subject);
        Minesweeper.isInFunctionList.remove(subject);
    }

}
