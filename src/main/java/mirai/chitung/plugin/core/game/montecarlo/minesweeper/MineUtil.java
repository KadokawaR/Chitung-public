package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import com.google.common.collect.ImmutableSet;
import mirai.chitung.plugin.core.game.montecarlo.MonteCarloUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MineUtil implements MonteCarloUtil<MineUserData> {

    static final String Rules = "里格斯公司邀请您参与本局扫雷，请在60秒之内输入 /bet+数字 参与游戏。输入“扫雷说明书”查看具体玩法。";
    static final String Stops = "本局扫雷已经取消。";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。如有多重下注，请使用空格隔开。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";
    static final String WrongStartNotice = "扫雷开局失败，请阅读扫雷说明书。";
    static final String OutOfBoundaryNotice = "扫雷数值设置超出范围，请阅读扫雷说明书。";

    static final int GapTime = 60;

    static Set<String> functionKeyWords = ImmutableSet.of(",", "，","random");

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean senderIsInGamingProcess(MessageEvent event){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.equals(event.getSender()) && data.subject.equals(event.getSubject())) return true;
            if(hasStarted(event.getSubject())) return true;
        }
        return false;
    }

    @Override
    public boolean subjectIsInGamingProcess(Contact subject){
        for (MineUserData data: Minesweeper.data){
            if(data.subject.equals(subject)) return true;
        }
        return false;
    }

    @Override
    public boolean hasStarted(Contact subject){
        for(Contact c:Minesweeper.startBetList){
            if(c.equals(subject)) return true;
        }
        return false;
    }

    @Override
    public int getBet(Contact sender,Contact subject){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.equals(sender)&&data.subject.equals(subject)) return data.bet;
        }
        return 0;
    }

    @Override
    public void addBet(Contact sender,int bet){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.equals(sender)) data.addBetAmount(bet);
        }
    }

    @Override
    public MineUserData getData(Contact sender){
        for (MineUserData data: Minesweeper.data){
            if(data.sender.equals(sender)) return data;
        }
        return null;
    }

    @Override
    public void deleteAllSubject(Contact subject){
        Minesweeper.data.removeIf(data -> data.subject.getId() == subject.getId());
    }

    @Override
    public List<MineUserData> getUserList(Contact subject){
        return Minesweeper.data.stream().filter(userData -> userData.subject.equals(subject)).collect(Collectors.toList());
    }

    @Override
    public void clear(Contact subject){
        deleteAllSubject(subject);
        Minesweeper.startBetList.remove(subject);
        Minesweeper.isInBetList.remove(subject);
        Minesweeper.isInFunctionList.remove(subject);
        Minesweeper.mines.remove(subject);
    }

    public static int[][] dataConvert(List<MineData> data,int x,int y){
        int[][] result = MineFactory.initializeArray(new int[x][y],0);
        for(MineData position:data){
            result[position.x-1][position.y-1] =1;
        }
        return result;
    }

    public static List<MineData> dataConvert(int[][] data,int x,int y){
        List<MineData> mineDataList = new ArrayList<>();
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[0].length;j++){
                if(data[i][j]==1) mineDataList.add(new MineData(i+1,j+1));
            }
        }
        return mineDataList;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean minesNumberCheck(int x, int y, int mines){
        return (mines>=1&&mines<=99&&mines<x*y-1);
    }

    public static boolean hasExploded(List<MineData> result, List<MineData> user){
        for(MineData data1:result){
            for(MineData data2:user){
                if(data1.equals(data2)) return true;
            }
        }
        return false;
    }

    public static List<MineData> getUserBetList(Contact subject){
        List<MineData> result = new ArrayList<>();
        for(MineUserData mud: Minesweeper.data){
            if(mud.subject.equals(subject)){
                for(MineData md: mud.betList){
                    if(!result.contains(md)) result.add(md);
                }
            }
        }
        return result;
    }

    public static double calculateOdd(Contact subject){

        List<MineData> totalBetList = getUserBetList(subject);
        int betNumber = totalBetList.size();
        MineSetting mineSetting = Minesweeper.mines.get(subject);

        double basicOdd = (double) mineSetting.mineNumber / (double) (mineSetting.x*mineSetting.y) ;
        double odd = Math.pow(1-basicOdd,betNumber);

        return ((int)(1/odd*100))/ (double) 100;

    }

}
