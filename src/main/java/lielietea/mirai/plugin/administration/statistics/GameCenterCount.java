package lielietea.mirai.plugin.administration.statistics;

import lielietea.mirai.plugin.utils.MessageUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameCenterCount {

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    final static GameCenterCount INSTANCE = new GameCenterCount();
    Map<Functions,Integer> countMap;

    GameCenterCount() {
        countMap = new HashMap<>();
        for(Functions funct:Functions.values()){
            countMap.put(funct,0);
        }
        executor.scheduleAtFixedRate(new AutoClear(),6,6, TimeUnit.HOURS);

    }
    static public GameCenterCount getINSTANCE() {
        return INSTANCE;
    }

    class AutoClear implements Runnable{
        @Override
        public void run() {
            MessageUtil.notifyDevGroup(getResult(), Bot.getInstances().get(0).getId());
            for(Functions funct:Functions.values()){
                countMap.put(funct,0);
            }
        }
    }

    public static void count(Functions function){
        getINSTANCE().countMap.put(function,getINSTANCE().countMap.get(function)+1);
    }

    public static String getResult(){
        StringBuilder sb = new StringBuilder();
        for(Functions funct:Functions.values()){
            sb.append(funct.name()).append(": ").append(getINSTANCE().countMap.get(funct)).append("\n");
        }
        return sb.toString();
    }

    public static void getStatistics(MessageEvent event){
        if(!event.getMessage().contentToString().contains("/gamecenter")) return;
        MessageUtil.notifyDevGroup(getResult(),Bot.getInstances().get(0).getId());
    }

    public enum Functions {
        JetpackInfo,
        JetpackStart,
        JetpackLocation,
        JetpackConfirmation,
        JetpackRecord,

        FishingInfo,
        FishingHandbook,
        FishingGo,
        FishingCollection,
        FishingNotReadyYet,

        BankCheck,

        BlackjackStart,
        BlackjackBet,
        BlackjackOperations,

        RouletteStart,
        RouletteBet,
        RouletteOperations,
    }

}
