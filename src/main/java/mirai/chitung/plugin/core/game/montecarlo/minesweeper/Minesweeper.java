package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import mirai.chitung.plugin.core.game.montecarlo.GeneralMonteCarloUtil;
import mirai.chitung.plugin.core.game.montecarlo.MonteCarloGame;
import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSai;
import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSaiUserData;
import mirai.chitung.plugin.core.game.montecarlo.taisai.TaiSaiUtil;
import mirai.chitung.plugin.utils.image.ImageSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Minesweeper implements MonteCarloGame<MessageEvent> {

    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    static CopyOnWriteArrayList<Contact> startBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInFunctionList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<MineUserData> data = new CopyOnWriteArrayList<>();

    @Override
    public void handle(MessageEvent event) {
        String message = event.getMessage().contentToString();
        if(matchFunction(message)) process(event,message);
    }

    @Override
    public synchronized void process(MessageEvent event, String message) {
        start(event,message);
        bet(event,message);
        function(event,message);
    }

    @Override
    public void start(MessageEvent event, String message) {
        if(!matchStart(message)) return;
        if(MineUtil.hasStarted(event.getSubject())||MineUtil.subjectIsInGamingProcess(event.getSubject())) return;

        executorService.schedule(new Start(event.getSubject()),MineUtil.GapTime, TimeUnit.SECONDS);

        MessageChainBuilder mcb = new MessageChainBuilder().append(MineUtil.Rules);
        event.getSubject().sendMessage(mcb.asMessageChain());

        startBetList.add(event.getSubject());
    }

    @Override
    public void function(MessageEvent event, String message) {

        //todo 下注交互

    }

    @Override
    public void bet(MessageEvent event, String message) {
        if(!matchBet(message)) return;

        if(isInFunctionList.contains(event.getSubject())) return;

        if(!startBetList.contains(event.getSubject())&&!isInBetList.contains(event.getSubject())) return;

        if(!GeneralMonteCarloUtil.checkBet(event,message,MineUtil.getBet(event.getSender(),event.getSubject()))) return;

        if(startBetList.contains(event.getSubject())){
            event.getSubject().sendMessage(MineUtil.StartBetNotice);
            executorService.schedule(new EndBet(event.getSubject()),MineUtil.GapTime, TimeUnit.SECONDS);
            startBetList.remove(event.getSubject());
            isInBetList.add(event.getSubject());
        }

        if(!TaiSaiUtil.senderIsInGamingProcess(event)) {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您已经下注").append(String.valueOf(bet)).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            data.add(new MineUserData(event,bet));
            startBetList.remove(event.getSubject());

        } else {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            MineUtil.addBet(event.getSender(),bet);
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您总共下注").append(String.valueOf(MineUtil.getBet(event.getSender(),event.getSubject()))).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());

        }
    }

    @Override
    public boolean matchStart(String message) {
        return message.equalsIgnoreCase("/minesweeper")||message.equals("扫雷");
    }

    @Override
    public boolean matchBet(String message) {
        return message.toLowerCase().startsWith("/bet") || message.startsWith("下注");
    }

    @Override
    public boolean matchFunction(String message) {
        for(String keywords:MineUtil.functionKeyWords){
            if(message.toLowerCase().contains(keywords)) return true;
        }
        return false;
    }

    @Override
    public boolean matchGame(String message) {
        return matchBet(message)||matchStart(message)||matchFunction(message);
    }

    @Override
    public boolean isInGamingProcess(MessageEvent event) {
        return MineUtil.subjectIsInGamingProcess(event.getSubject())||MineUtil.hasStarted(event.getSubject());
    }

    static class Start implements Runnable{

        private final Contact subject;

        Start(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){

            if(startBetList.contains(subject)){
                startBetList.remove(subject);
                MineUtil.deleteAllSubject(subject);
                subject.sendMessage(MineUtil.Stops);
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
            isInBetList.remove(subject);
            isInFunctionList.add(subject);
            subject.sendMessage(MineUtil.EndBetNotice+MineUtil.StartOperateNotice);
            executorService.schedule(new EndFunction(subject),MineUtil.GapTime,TimeUnit.SECONDS);
        }
    }

    static class EndFunction implements Runnable{

        private final Contact subject;

        EndFunction(Contact subject){
            this.subject=subject;
        }

        @Override
        public void run(){

            isInFunctionList.remove(subject);


            //todo 生成结果

            try {

                //todo 发送图片

            } catch(Exception e){
                e.printStackTrace();
            }

            MessageChainBuilder mcb = new MessageChainBuilder().append(MineUtil.EndGameNotice).append("\n");

            for(MineUserData data:MineUtil.getUserList(subject)){

                if(data.betList.size()==0) continue;

                if(data.isGroup()){
                    mcb.append("\n").append(new At(data.sender.getId()));
                } else {
                    mcb.append("\n您");
                }

                //todo 计算倍率和钱
                int money = 0;
                double times = 0;

                mcb.append("获得了").append(String.valueOf(money)).append("南瓜比索，总倍率为×").append(String.format("%.1f", times)).append("。");
            }

            subject.sendMessage(mcb.asMessageChain());

            MineUtil.clear(subject);

        }
    }
}
