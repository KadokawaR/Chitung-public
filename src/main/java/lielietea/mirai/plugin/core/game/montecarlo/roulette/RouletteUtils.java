package lielietea.mirai.plugin.core.game.montecarlo.roulette;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.*;

public class RouletteUtils extends IndicatorProcessor{

    static boolean isRoulette(MessageEvent event){
        return (event.getMessage().contentToString().equals("/roulette")||event.getMessage().contentToString().equals("轮盘"));
    }

    static boolean isBet(MessageEvent event){
        return (event.getMessage().contentToString().contains("/bet")||event.getMessage().contentToString().contains("下注"));
    }

    static Integer getBet(String string){
        string = string.replace(" ","");
        string = string.replace("/bet","");
        string = string.replace("下注","");
        Integer res;
        try{res = Integer.parseInt(string);}
        catch (NumberFormatException e){
            e.printStackTrace();
            res=null;
        }
        return res;
    }

    //查看列表里面是不是都是Wrong
    static boolean isAllWrong(List<RouletteBet> betList){
        for(RouletteBet rb: betList){
            if (rb.indicator.equals(Indicator.Wrong)) continue;
            if (rb.status.equals(Status.Cool)) return false;
        }
        return true;
    }

    //查看列表里面有多少个下注
    static int getBetAmount(List<RouletteBet> betList){
        int time=0;
        for(RouletteBet rb: betList){
            if(!rb.indicator.equals(Indicator.Wrong)&&(rb.status.equals(Status.Cool))) time++;
        }
        return time;
    }

    //整理实际的下注
    static List<RouletteBet> getDeFactoBets(List<RouletteBet> betList){
        List<RouletteBet> resBetList = new ArrayList<>();
        for(RouletteBet rb: betList){
            if(!rb.indicator.equals(Indicator.Wrong)&&(rb.status.equals(Status.Cool))) resBetList.add(rb);
        }
        return resBetList;
    }

    //告知下注情况
    static String feedbeckBetStatus(List<RouletteBet> betList){
        if(isAllWrong(betList)) return "未获得有效下注。";
        StringBuilder sb = new StringBuilder();
        sb.append("已收到：\n");
        boolean hasUsedWrongIndicator = false;
        for(RouletteBet rb: betList){
            if(rb.indicator.equals(Indicator.Wrong)){
                hasUsedWrongIndicator = true;
                continue;
            }
            if(rb.status.equals(Status.WrongNumber)){
                sb.append(rb.indicator.getName()).append("的数字下注不正确。\n");
                continue;
            }
            if(doesntNeedNumber(rb.indicator)){
                sb.append(rb.indicator.getName()).append("\n");
            } else {
                sb.append("下在").append(rb.location).append("的").append(rb.indicator.getName()).append("\n");
            }
        }
        if(hasUsedWrongIndicator){
            sb.append("\n存在指示器使用错误，请仔细阅读说明书。");
        }
        return sb.toString();
    }

    //设置FriendSettleAccount
    static void setNewTableForFriend(MessageEvent event){
        for(int i=0;i<37;i++){
            Roulette.getINSTANCE().FriendSettleAccount.put(event.getSubject().getId(),i,0);
        }
    }

    //设置GroupSettleAccount
    static void setNewMapForGroup(MessageEvent event){
        Table <Long,Integer,Integer> tempTable = HashBasedTable.create();
        for(int i=0;i<37;i++){
            tempTable.put(event.getSender().getId(),i,0);
        }
        if(!Roulette.getINSTANCE().GroupSettleAccount.containsKey(event.getSubject().getId())) {
            Roulette.getINSTANCE().GroupSettleAccount.put(event.getSubject().getId(), tempTable);
            return;
        }
        for(int i=0;i<37;i++) {
            Roulette.getINSTANCE().GroupSettleAccount.get(event.getSubject().getId()).put(event.getSender().getId(),i,0);
        }
    }

    //根据下注往Table里面塞赌注 Friend
    static void updateTable(List<RouletteBet> rouletteBetList, long ID){
        Table<Long,Integer,Integer> newTable;
        newTable = Roulette.getINSTANCE().FriendSettleAccount;
        for(RouletteBet rb:rouletteBetList){
            Set<Integer> someSet;
            someSet=getSet(rb.location,rb.indicator);
            for (int itNext : someSet) {
                Integer originalValue = newTable.get(ID, itNext);
                if(originalValue==null) originalValue=0;
                newTable.put(ID, itNext, originalValue + rb.indicator.getTime());
            }
        }
    }

    //根据下注往Map里面塞赌注 Group
    static void updateMap(List<RouletteBet> rouletteBetList, long playerID, long groupID){
        for(RouletteBet rb:rouletteBetList){
            Set<Integer> someSet;
            someSet=getSet(rb.location,rb.indicator);
            for (int itNext : someSet) {
                Integer originalValue = Roulette.getINSTANCE().GroupSettleAccount.get(groupID).get(playerID, itNext);
                if(originalValue==null) originalValue=0;
                Roulette.getINSTANCE().GroupSettleAccount.get(groupID).put(playerID, itNext, rb.indicator.getTime() + originalValue);
            }
        }
    }

    static Set<Integer> getSet(Integer location,Indicator indicator){
        Set<Integer> someSet = new HashSet<>();
        System.out.println("开始获得 Set<Integer>");
        System.out.println("指示器是"+indicator+" 位置是"+location);
        switch(indicator){
            case Black: someSet.addAll(RouletteAreas.Black); break;
            case Red: someSet.addAll(RouletteAreas.Red); break;
            case Odd: someSet.addAll(RouletteAreas.getOddArea()); break;
            case Even: someSet.addAll(RouletteAreas.getEvenArea()); break;
            case Line: someSet.addAll(RouletteAreas.getLineArea(location)); break;
            case Column: someSet.addAll(RouletteAreas.getColumnArea(location)); break;
            case Four: someSet.addAll(RouletteAreas.getFourArea(location)); break;
            case Six: someSet.addAll(RouletteAreas.getSixArea(location)); break;
            case Part: someSet.addAll(RouletteAreas.getPartArea(location)); break;
            case Half: someSet.addAll(RouletteAreas.getHalfArea(location)); break;
            case Number: someSet.add(location); break;
        }
        return someSet;
    }

}
