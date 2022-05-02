package mirai.chitung.plugin.core.game.montecarlo.taisai;

import com.google.common.collect.ImmutableSet;
import mirai.chitung.plugin.core.game.montecarlo.MonteCarloUtil;
import mirai.chitung.plugin.utils.image.ImageCreater;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class TaiSaiUtil implements MonteCarloUtil<TaiSaiUserData> {

    static final String TaiSaiRules = "里格斯公司邀请您参与本局骰宝，请在60秒之内输入 /bet+数字 参与游戏。";
    static final String TaiSaiStops = "本局骰宝已经取消。";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。\n在这一阶段不会向您收取南瓜比索。由于骰宝可以多重下注，因此不建议设置过大的 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。如有多重下注，请使用空格隔开。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";

    static final int GapTime = 60;

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

    @Override
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean senderIsInGamingProcess(MessageEvent event){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.equals(event.getSender()) && tsud.subject.equals(event.getSubject())) return true;
            if(hasStarted(event.getSubject())) return true;
        }
        return false;
    }

        @Override
        public boolean subjectIsInGamingProcess(Contact subject){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.subject.equals(subject)) return true;
        }
        return false;
    }

    @Override
    public  boolean hasStarted(Contact subject){
        for(Contact c:TaiSai.startBetList){
            if(c.equals(subject)) return true;
        }
        return false;
    }

    @Override
    public  int getBet(Contact sender,Contact subject){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.equals(sender)&&tsud.subject.equals(subject)) return tsud.bet;
        }
        return 0;
    }

    @Override
    public  void addBet(Contact sender,int bet){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.equals(sender)) tsud.addBetAmount(bet);
        }
    }

    @Override
    public  TaiSaiUserData getData(Contact sender){
        for (TaiSaiUserData tsud:TaiSai.data){
            if(tsud.sender.equals(sender)) return tsud;
        }
        return null;
    }

    @Override
    public  void deleteAllSubject(Contact subject){
        TaiSai.data.removeIf(data -> data.subject.getId() == subject.getId());
    }

    @Override
    public  List<TaiSaiUserData> getUserList(Contact subject){
        return TaiSai.data.stream().filter(userData -> userData.subject.equals(subject)).collect(Collectors.toList());
    }

    @Override
    public  void clear(Contact subject){
        deleteAllSubject(subject);
        TaiSai.startBetList.remove(subject);
        TaiSai.isInBetList.remove(subject);
        TaiSai.isInFunctionList.remove(subject);
    }

    static String getTimes(TaiSaiData tsd){
        return "×"+String.format("%.1f", getTimesAsDouble(tsd));
    }

    static double getTimesAsDouble(TaiSaiData tsd) {
        switch (tsd.type) {
            case Number:
                switch (tsd.specificNumber) {
                    case 3:
                    case 18:
                        return 216;
                    case 4:
                    case 17:
                        return 72;
                    case 5:
                    case 16:
                        return 36;
                    case 6:
                    case 15:
                        return 21.6;
                    case 7:
                    case 14:
                        return 14.4;
                    case 8:
                    case 13:
                        return 10.2;
                    case 9:
                    case 12:
                        return 8.6;
                    case 10:
                    case 11:
                        return 8;
                }
            case Big:
                return 2;
            case Small:
                return 2;
            case Double:
                return 13.5;
            case Triple:
                return 216;
            case AllTriple:
                return 36;
        }
        return 0;
    }

    static double calculator(int[] result,TaiSaiUserData tsud){

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
                                times+=10.2;
                                break;
                            case 9:
                            case 12:
                                times+=8.6;
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

    static BufferedImage createImage(int[] nums){
        String path = "/pics/casino/taisai/";
        BufferedImage[] bufferedImages = new BufferedImage[nums.length];
        for(int i=0;i<nums.length;i++){
            bufferedImages[i] = ImageCreater.getImageFromResource(path+nums[i]+".png");
        }

        return ImageCreater.addImagesVertically(bufferedImages);
    }

}
