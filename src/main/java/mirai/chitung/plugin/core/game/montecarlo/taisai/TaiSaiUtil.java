package mirai.chitung.plugin.core.game.montecarlo.taisai;

import com.google.common.collect.ImmutableSet;
import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TaiSaiUtil {

    static final String TaiSaiRules = "里格斯公司邀请您参与本局骰宝，请在60秒之内输入 /bet+数字 参与游戏。";
    static final String TaiSaiStops = "本局骰宝已经取消。";
    static final String NotRightBetNumber = "/bet 指令不正确，请重新再尝试";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。\n在这一阶段不会向您收取南瓜比索。由于骰宝可以多重下注，因此不建议设置过大的 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。如有多重下注，请使用空格隔开。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";

    static final int GapTime = 25;
    static Lock lock = new ReentrantLock();

    static List<String> HanziNumber = Arrays.asList("十","一","二","三","四","五","六","七","八","九");

    static Set<String> functionKeyWords = ImmutableSet.of(
            "围",
            "大",
            "小",
            "点",
            "/betbig",
            "/betsmall",
            "对"
    );

    public static String convertToArabicNumber(String content) {

        content = content.replaceAll("一", "1")
                .replaceAll("二", "2")
                .replaceAll("三", "3")
                .replaceAll("四", "4")
                .replaceAll("五", "5")
                .replaceAll("六", "6")
                .replaceAll("七", "7")
                .replaceAll("八", "8")
                .replaceAll("九", "9")
                .replaceAll("十", "1");

        return content;
    }

    public static String fromArabicNumberToHanzi(int num){
        if(num<10&&num>0){
            return HanziNumber.get(num);
        }

        String result = "";
        result+=HanziNumber.get(num/10);
        result+=HanziNumber.get(num%10);
        return result;
    }

    public static String process(String content) {
        content = convertToArabicNumber(content);
        return content;
    }

    static boolean matchStart(String content){
        return content.equalsIgnoreCase("/taisai")||content.equals("骰宝")||content.equals("买大小");
    }

    static boolean matchBet(String content){
        return content.toLowerCase().startsWith("/bet")||content.startsWith("下注");
    }

    static boolean matchFunction(String content){
        for(String keywords:functionKeyWords){
            if(content.toLowerCase().contains(keywords)) return true;
        }
        return false;
    }

    public static boolean isInGamingProcess(MessageEvent event){
        return subjectIsInGamingProcess(event.getSubject())||hasStarted(event.getSubject());
    }

    public static boolean senderIsInGamingProcess(MessageEvent event){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.getId()==event.getSender().getId() && tsud.subject.getId()==event.getSubject().getId()) return true;
            if(hasStarted(event.getSubject())) return true;
        }
        return false;
    }

    static boolean subjectIsInGamingProcess(Contact subject){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.subject.getId()==subject.getId()) return true;
        }
        return false;
    }

    static boolean hasStarted(Contact subject){
        for(Contact c:TaiSai.startBetList){
            if(c.getId()==subject.getId()) return true;
        }
        return false;
    }

    static int getBet(Contact sender,Contact subject){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.getId()==sender.getId()&&tsud.subject.getId()==subject.getId()) return tsud.bet;
        }
        return 0;
    }

    static void addBet(Contact sender,int bet){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.getId()==sender.getId()) tsud.addBetAmount(bet);
        }
    }

    static TaiSaiUserData getData(Contact sender){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.getId()==sender.getId()) return tsud;
        }
        return null;
    }

    static void deleteAllSubject(Contact subject){
        List<TaiSaiUserData> clonedList = new ArrayList<>(TaiSai.data);
        for(TaiSaiUserData tsud:clonedList){
            if(tsud.subject.getId()==subject.getId()) TaiSai.data.remove(tsud);
        }
    }

    static List<TaiSaiUserData> getTaiSaiUserList(Contact subject){
        return TaiSai.data.stream().filter(userData -> userData.subject.getId()==subject.getId()).collect(Collectors.toList());
    }

    static void clear(Contact subject){
        deleteAllSubject(subject);
        TaiSai.startBetList.remove(subject);
        TaiSai.isInFunctionList.remove(subject);
    }

    static long calculator(int[] result,TaiSaiUserData tsud){

        long times = 0;
        int total = result[0]+result[1]+result[2];
        boolean isTriple = result[0]==result[1]&&result[1]==result[2];

        for(TaiSaiData tsd: tsud.betList){
            switch(tsd.type){
                case Number:
                    if (total==tsd.specificNumber){
                        switch (tsd.specificNumber){
                            case 3:
                            case 18:
                                times+=216;
                                break;
                            case 4:
                            case 17:
                                times+=72;
                                break;
                            case 5:
                            case 16:
                                times+=36;
                                break;
                            case 6:
                            case 15:
                                times+=21.6;
                                break;
                            case 7:
                            case 14:
                                times+=14.4;
                                break;
                            case 8:
                            case 13:
                                times+=10.285;
                                break;
                            case 9:
                            case 12:
                                times+=8.64;
                                break;
                            case 10:
                            case 11:
                                times+=8;
                                break;
                        }
                    }
                    break;

                case Big:
                    if(isTriple) break;
                    if (total>=11) times+=2;
                    break;

                case Small:
                    if(isTriple) break;
                    if (total<=10){
                        times+=2;
                    }

                case Double:
                    int count = 0;
                    for(int i:result){
                        if(i==tsd.specificNumber){
                            count++;
                        }
                    }
                    if(count>1){
                        times+=13.5;
                        break;
                    }
                    break;

                case Triple:
                    if(isTriple&&result[0]==tsd.specificNumber){
                        times+=216;
                        break;
                    }
                    break;

                case AllTriple:
                    if(isTriple){
                        times+=36;
                        break;
                    }
                    break;
            }
        }

        return times;

    }

    static class Start implements Runnable{

        private final Contact subject;

        Start(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){

            if(TaiSai.startBetList.contains(subject)){
                TaiSai.startBetList.remove(subject);
                deleteAllSubject(subject);
                subject.sendMessage(TaiSaiStops);
            }
        }
    }

    static class EndBet implements Runnable{

        private final Contact subject;

        EndBet(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){
            TaiSai.isInBetList.remove(subject);
            TaiSai.isInFunctionList.add(subject);
            subject.sendMessage(EndBetNotice+StartOperateNotice);
            TaiSai.executorService.schedule(new EndFunction(subject),GapTime,TimeUnit.SECONDS);
        }
    }

    static class EndFunction implements Runnable{

        private final Contact subject;

        EndFunction(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){

            int[] result = new int[3];

            for(int i=0;i<3;i++){
                result[i] = new Random().nextInt(6)+1;
            }

            subject.sendMessage(result[0]+" "+result[1]+" "+result[2]);

            MessageChainBuilder mcb = new MessageChainBuilder().append(EndGameNotice).append("\n");

            for(TaiSaiUserData tsud:getTaiSaiUserList(subject)){

                if(tsud.betList.size()==0) continue;

                if(tsud.isGroup()){
                    mcb.append("\n").append(new At(tsud.sender.getId()));
                } else {
                    mcb.append("\n您");
                }
                int money = Math.toIntExact(calculator(result, tsud) * tsud.bet);
                PumpkinPesoWindow.addMoney(tsud.sender.getId(),money);
                mcb.append("获得了").append(String.valueOf(money)).append("南瓜比索");
            }

            subject.sendMessage(mcb.asMessageChain());

            clear(subject);

        }
    }

}
