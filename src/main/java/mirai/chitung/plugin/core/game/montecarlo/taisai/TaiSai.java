package mirai.chitung.plugin.core.game.montecarlo.taisai;

import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import mirai.chitung.plugin.core.game.montecarlo.GeneralMonteCarloUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class TaiSai extends TaiSaiUtil{

    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    static CopyOnWriteArrayList<Contact> startBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInFunctionList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<TaiSaiUserData> data = new CopyOnWriteArrayList<>();


    public static void handle(MessageEvent event){
        String content = event.getMessage().contentToString();
        start(content, event);
        bet(content, event);
        function(content, event);
    }

    static void bet(String content,MessageEvent event){

        if(!matchBet(content)) return;

        if(isInFunctionList.contains(event.getSubject())) return;

        if(!startBetList.contains(event.getSubject())&&!isInBetList.contains(event.getSubject())) return;

        if(!GeneralMonteCarloUtil.checkBet(event,content,getBet(event.getSender(),event.getSubject()))) return;

        if(startBetList.contains(event.getSubject())){
            event.getSubject().sendMessage(StartBetNotice);
            executorService.schedule(new EndBet(event.getSubject()),GapTime, TimeUnit.SECONDS);
            startBetList.remove(event.getSubject());
            isInBetList.add(event.getSubject());
        }

        if(!senderIsInGamingProcess(event)) {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(content));
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您已经下注").append(String.valueOf(bet)).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            data.add(new TaiSaiUserData(event,bet));
            startBetList.remove(event.getSubject());

        } else {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(content));
            addBet(event.getSender(),bet);
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您总共下注").append(String.valueOf(getBet(event.getSender(),event.getSubject()))).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());

        }

    }

    static void start(String content,MessageEvent event){
        if(!matchStart(content)) return;
        if(hasStarted(event.getSubject())||subjectIsInGamingProcess(event.getSubject())) return;
        executorService.schedule(new Start(event.getSubject()),GapTime,TimeUnit.SECONDS);
        event.getSubject().sendMessage(TaiSaiRules);
        startBetList.add(event.getSubject());
    }

    public static void function(String content,MessageEvent event){

        if(!isInFunctionList.contains(event.getSubject())) return;

        List<TaiSaiData> functions = new ArrayList<>();

        String[] elements = content.toLowerCase().split(" ");
        int illegalIndicator = 0;

        for (String element : elements) {
            element = TaiSaiUtil.process(element);

            System.out.println(element);

            if (element.startsWith("对")) {
                Integer num =null;
                try { num = Integer.parseInt(Pattern.compile("[^0-9]").matcher(element).replaceAll(" ").trim()); } catch (Exception e) { e.printStackTrace(); }
                if (num == null) {
                    illegalIndicator++;
                    continue;
                }
                if (num>6||num<1) {
                    illegalIndicator++;
                    continue;
                }
                functions.add(new TaiSaiData(num,TaiSaiBetType.Double));
                continue;
            }

            if (element.startsWith("围")){
                Integer num =null;
                try { num = Integer.parseInt(Pattern.compile("[^0-9]").matcher(element).replaceAll(" ").trim()); } catch (Exception e) { e.printStackTrace(); }
                if (num == null) {
                    illegalIndicator++;
                    continue;
                }
                if (num>6||num<1) {
                    illegalIndicator++;
                    continue;
                }
                functions.add(new TaiSaiData(num,TaiSaiBetType.Triple));
                continue;
            }

            if (element.equals("买大")||element.equals("big")||element.equals("大")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.Big));
                continue;
            }

            if (element.equals("买小")||element.equals("small")||element.equals("小")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.Small));
                continue;
            }

            if (element.equals("全围")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.AllTriple));
                continue;
            }

            if(element.endsWith("点")){
                Integer num =null;
                System.out.println(element);
                element = Pattern.compile("[^0-9]").matcher(element).replaceAll(" ").trim();
                try { num = Integer.parseInt(element);System.out.println(num); } catch (Exception e) { e.printStackTrace(); }
                if (num == null) {
                    illegalIndicator++;
                    continue;
                }
                if (num>18||num<3) {
                    illegalIndicator++;
                    continue;
                }
                functions.add(new TaiSaiData(num,TaiSaiBetType.Number));
                continue;
            }

            illegalIndicator++;

        }

        MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event);

        if(functions.size()==0 && illegalIndicator==0){
            mcb.append("未收到任何有效下注。请仔细阅读说明书。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(functions.size()==0){
            mcb.append("未收到任何有效下注，").append("存在").append(String.valueOf(illegalIndicator)).append("处指示器使用错误，请仔细阅读说明书。");
            return;
        }

        int totalAmount = getBet(event.getSender(),event.getSubject())*functions.size();

        if(!GeneralMonteCarloUtil.hasEnoughMoney(event,totalAmount)){
            mcb.append(YouDontHaveEnoughMoney).append("请尝试减少单次的下注数量。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(getData(event.getSender())!=null) Objects.requireNonNull(getData(event.getSender())).addBet(functions);

        PumpkinPesoWindow.minusMoney(event.getSender().getId(),totalAmount);

        mcb.append("已收到如下下注：\n");
        StringBuilder sb = new StringBuilder();

        for(TaiSaiData tsd:functions) {

            switch (tsd.type) {
                case AllTriple:
                    sb.append("全围 ");
                    break;
                case Triple:
                    sb.append("围").append(fromArabicNumberToHanzi(tsd.specificNumber)).append(" ");
                    break;
                case Double:
                    sb.append("对").append(fromArabicNumberToHanzi(tsd.specificNumber)).append(" ");
                    break;
                case Small:
                    sb.append("买小 ");
                    break;
                case Big:
                    sb.append("买大 ");
                    break;
                case Number:
                    sb.append(tsd.specificNumber).append("点 ");
                    break;
            }

        }

        mcb.append(sb.toString().trim());

        if(illegalIndicator>0) {
            mcb.append("\n存在").append(String.valueOf(illegalIndicator)).append("处指示器使用错误，请仔细阅读说明书。");
        }

        mcb.append("\n共花费").append(String.valueOf(totalAmount)).append("南瓜比索。");

        event.getSubject().sendMessage(mcb.asMessageChain());
    }

}
