package mirai.chitung.plugin.core.game.montecarlo.taisai;

import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import mirai.chitung.plugin.core.game.montecarlo.GeneralMonteCarloUtil;
import mirai.chitung.plugin.core.game.montecarlo.MonteCarloGame;
import mirai.chitung.plugin.core.game.montecarlo.blackjack.BlackJack;
import mirai.chitung.plugin.utils.image.ImageSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class TaiSai implements MonteCarloGame<MessageEvent> {

    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    static CopyOnWriteArrayList<Contact> startBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInFunctionList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<TaiSaiUserData> data = new CopyOnWriteArrayList<>();

    static final String TAISAI_PATH = "/pics/casino/taisai.png";

    public TaiSai(){}

    @Override
    public void handle(MessageEvent event) {
        String message = event.getMessage().contentToString();
        if(matchGame(message)) process(event,message);
    }


    @Override
    public synchronized void process(MessageEvent event, String message){
        start(event,message);
        bet(event,message);
        function(event,message);
    }


    @Override
    public void start(MessageEvent event, String message) {
        if(!matchStart(message)) return;
        if(TaiSaiUtil.hasStarted(event.getSubject())||TaiSaiUtil.subjectIsInGamingProcess(event.getSubject())) return;

        executorService.schedule(new Start(event.getSubject()),TaiSaiUtil.GapTime,TimeUnit.SECONDS);

        MessageChainBuilder mcb = new MessageChainBuilder().append(TaiSaiUtil.TaiSaiRules);
        InputStream img = BlackJack.class.getResourceAsStream(TAISAI_PATH);
        assert img != null;

        mcb.append("\n\n").append(Contact.uploadImage(event.getSubject(), img));
        event.getSubject().sendMessage(mcb.asMessageChain());

        startBetList.add(event.getSubject());
    }

    @Override
    public void function(MessageEvent event, String message) {

        if(!isInFunctionList.contains(event.getSubject())) return;

        if(!TaiSaiUtil.senderIsInGamingProcess(event)) return;

        List<TaiSaiData> functions = new ArrayList<>();

        String[] elements = message.toLowerCase().split(" ");
        int illegalIndicator = 0;

        for (String element : elements) {
            element = TaiSaiUtil.process(element);

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

            if (element.equals("买大")||element.equalsIgnoreCase("big")||element.equals("大")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.Big));
                continue;
            }

            if (element.equals("买小")||element.equalsIgnoreCase("small")||element.equals("小")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.Small));
                continue;
            }

            if (element.equals("全围")){
                functions.add(new TaiSaiData(-1,TaiSaiBetType.AllTriple));
                continue;
            }

            if(element.endsWith("点")){
                Integer num =null;
                element = Pattern.compile("[^0-9]").matcher(element).replaceAll(" ").trim();
                try { num = Integer.parseInt(element);} catch (Exception e) { e.printStackTrace(); }
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

        int totalAmount = TaiSaiUtil.getBet(event.getSender(),event.getSubject())*functions.size();

        if(!GeneralMonteCarloUtil.hasEnoughMoney(event,totalAmount)){
            mcb.append(TaiSaiUtil.YouDontHaveEnoughMoney).append("请尝试减少单次的下注数量。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(TaiSaiUtil.getData(event.getSender())!=null) Objects.requireNonNull(TaiSaiUtil.getData(event.getSender())).addBet(functions);

        PumpkinPesoWindow.minusMoney(event.getSender().getId(),totalAmount);

        mcb.append("已收到如下下注：\n");
        StringBuilder sb = new StringBuilder();

        for(TaiSaiData tsd:functions) {

            switch (tsd.type) {
                case AllTriple:
                    sb.append("全围（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
                    break;
                case Triple:
                    sb.append("围").append(TaiSaiUtil.fromArabicNumberToHanzi(tsd.specificNumber)).append("（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
                    break;
                case Double:
                    sb.append("对").append(TaiSaiUtil.fromArabicNumberToHanzi(tsd.specificNumber)).append("（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
                    break;
                case Small:
                    sb.append("买小（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
                    break;
                case Big:
                    sb.append("买大（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
                    break;
                case Number:
                    sb.append(tsd.specificNumber).append("点（").append(TaiSaiUtil.getTimes(tsd)).append("） ");
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

    @Override
    public void bet(MessageEvent event, String message) {
        if(!matchBet(message)) return;

        if(isInFunctionList.contains(event.getSubject())) return;

        if(!startBetList.contains(event.getSubject())&&!isInBetList.contains(event.getSubject())) return;

        if(!GeneralMonteCarloUtil.checkBet(event,message,TaiSaiUtil.getBet(event.getSender(),event.getSubject()))) return;

        if(startBetList.contains(event.getSubject())){
            event.getSubject().sendMessage(TaiSaiUtil.StartBetNotice);
            executorService.schedule(new EndBet(event.getSubject()),TaiSaiUtil.GapTime, TimeUnit.SECONDS);
            startBetList.remove(event.getSubject());
            isInBetList.add(event.getSubject());
        }

        if(!TaiSaiUtil.senderIsInGamingProcess(event)) {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您已经下注").append(String.valueOf(bet)).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            data.add(new TaiSaiUserData(event,bet));
            startBetList.remove(event.getSubject());

        } else {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            TaiSaiUtil.addBet(event.getSender(),bet);
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您总共下注").append(String.valueOf(TaiSaiUtil.getBet(event.getSender(),event.getSubject()))).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());

        }
    }

    @Override
    public boolean matchStart(String message) {
        return message.equalsIgnoreCase("/taisai")||message.equals("骰宝")||message.equals("买大小")||message.equals("/sicbo");
    }

    @Override
    public boolean matchBet(String message) {
        return message.toLowerCase().startsWith("/bet") || message.startsWith("下注");
    }

    @Override
    public boolean matchFunction(String message) {
        for(String keywords:TaiSaiUtil.functionKeyWords){
            if(message.toLowerCase().contains(keywords)) return true;
        }
        return false;
    }

    @Override
    public boolean matchGame(String message){
        return matchBet(message)||matchStart(message)||matchFunction(message);
    }

    @Override
    public boolean isInGamingProcess(MessageEvent event) {
        return TaiSaiUtil.subjectIsInGamingProcess(event.getSubject())||TaiSaiUtil.hasStarted(event.getSubject());
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
                TaiSaiUtil.deleteAllSubject(subject);
                subject.sendMessage(TaiSaiUtil.TaiSaiStops);
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
            subject.sendMessage(TaiSaiUtil.EndBetNotice+TaiSaiUtil.StartOperateNotice);
            TaiSai.executorService.schedule(new EndFunction(subject),TaiSaiUtil.GapTime,TimeUnit.SECONDS);
        }
    }

    static class EndFunction implements Runnable{

        private final Contact subject;

        EndFunction(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){

            TaiSai.isInFunctionList.remove(subject);

            int[] result = new int[3];

            for(int i=0;i<3;i++){
                result[i] = new Random().nextInt(6)+1;
            }


            try {

                BufferedImage image = TaiSaiUtil.createImage(result);
                ImageSender.sendImageFromBufferedImage(subject, image);

            } catch(Exception e){
                e.printStackTrace();
            }

            MessageChainBuilder mcb = new MessageChainBuilder().append(TaiSaiUtil.EndGameNotice).append("\n");

            for(TaiSaiUserData tsud:TaiSaiUtil.getUserList(subject)){

                if(tsud.betList.size()==0) continue;

                if(tsud.isGroup()){
                    mcb.append("\n").append(new At(tsud.sender.getId()));
                } else {
                    mcb.append("\n您");
                }
                double times = TaiSaiUtil.calculator(result, tsud);
                int money = (int) (TaiSaiUtil.calculator(result, tsud) * tsud.bet);
                PumpkinPesoWindow.addMoney(tsud.sender.getId(),money);
                mcb.append("获得了").append(String.valueOf(money)).append("南瓜比索，总倍率为×").append(String.format("%.1f", times)).append("。");
            }

            subject.sendMessage(mcb.asMessageChain());

            TaiSaiUtil.clear(subject);

        }
    }

}
