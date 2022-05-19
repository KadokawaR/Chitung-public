package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import mirai.chitung.plugin.core.game.montecarlo.GeneralMonteCarloUtil;
import mirai.chitung.plugin.core.game.montecarlo.MonteCarloGame;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperFace;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperPoolType;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.imageutil.MinesweeperImage;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.imageutil.MinesweeperImageUtil;
import mirai.chitung.plugin.utils.image.ImageSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class Minesweeper implements MonteCarloGame<MessageEvent> {

    static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    static CopyOnWriteArrayList<Contact> startBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInBetList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<Contact> isInFunctionList = new CopyOnWriteArrayList<>();
    static CopyOnWriteArrayList<MineUserData> data = new CopyOnWriteArrayList<>();
    static ConcurrentHashMap<Contact,MineSetting> mines = new ConcurrentHashMap<>();

    public static MineUtil mineUtil = new MineUtil();

    @Override
    public void handle(MessageEvent event) {
        String message = event.getMessage().contentToString();
        if(matchGame(message)) process(event,message);
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
        if(mineUtil.hasStarted(event.getSubject())||mineUtil.subjectIsInGamingProcess(event.getSubject())) return;
        if(message.equals("扫雷说明书")||message.equalsIgnoreCase("minesweeper introduction")||message.equalsIgnoreCase("minesweeper -h")||message.equalsIgnoreCase("扫雷 -h")) return;


        String rawString = message.replace("扫雷","").replace("/minesweeper","").trim();

        boolean hasSet = false;

        switch(rawString.toLowerCase().replaceAll(" ","")){
            case "简单":
            case "初级":
            case "easy":
            case "beginner":
            case "-e":
            case "-b":
                mines.put(event.getSubject(), new MineSetting(9,9,10));
                hasSet = true;
                break;
            case "中级":
            case "intermediate":
            case "middle":
            case "-i":
                mines.put(event.getSubject(),new MineSetting(16,16,40));
                hasSet = true;
                break;
            case "高级":
            case "advanced":
            case "hard":
            case "-a":
                mines.put(event.getSubject(),new MineSetting(30,16,99));
                hasSet = true;
                break;
        }

        if(!hasSet){

            String[] undefined = rawString.split(" ");

            if(undefined.length!=3) return;

            int x = 0;
            int y = 0;
            int mine = 0;

            try {

                x=Integer.parseInt(undefined[0]);
                y=Integer.parseInt(undefined[1]);
                mine=Integer.parseInt(undefined[2]);

            } catch(Exception e) {
                e.printStackTrace();
            }

            if(x*y*mine==0) return;

            if(x<8||x>30||y<8||y>16||!MineUtil.minesNumberCheck(x,y,mine)){
                event.getSubject().sendMessage(MineUtil.OutOfBoundaryNotice);
                return;
            }

            mines.remove(event.getSubject());
            mines.put(event.getSubject(), new MineSetting(x,y,mine));

        }

        executorService.schedule(new Start(event.getSubject()), MineUtil.GapTime, TimeUnit.SECONDS);

        MessageChainBuilder mcb = new MessageChainBuilder().append(MineUtil.Rules);

        MineSetting mineSetting = mines.get(event.getSubject());

        BufferedImage bi = MinesweeperImage.assembleStructure(
                mineSetting.x,
                mineSetting.y,
                MineFactory.intToArray(mineSetting.mineNumber),
                MineFactory.doubleToArray(mineSetting.odd),
                MinesweeperFace.Normal);

        MinesweeperImage.addPool(bi,
                mineSetting.x,
                mineSetting.y,
                MinesweeperPoolType.NewPool,
                null,null);

        BufferedImage result = MinesweeperImageUtil.drawPureGreenBackground(bi);

        mcb.append(Contact.uploadImage(event.getSubject(), ImageSender.getBufferedImageAsSource(result)));
        event.getSubject().sendMessage(mcb.asMessageChain());

        startBetList.add(event.getSubject());

    }

    @Override
    public void function(MessageEvent event, String message) {

        if(!isInFunctionList.contains(event.getSubject())) return;

        if(!mineUtil.senderIsInGamingProcess(event)) return;

        List<MineData> positions = new ArrayList<>();

        int randomCount = 0;

        String[] elements = message.toLowerCase().split(" ");
        int illegalIndicator = 0;
        int repeatedValue = 0;

        MineSetting mineSetting = mines.get(event.getSubject());

        for (String element : elements) {

            if(element.toLowerCase().startsWith("random")){

                String number = element.replace("random","");
                int integer = 0;

                try{
                    integer=Integer.parseInt(number);
                } catch(Exception e){
                    e.printStackTrace();
                }

                if(integer<1||!MineUtil.minesNumberCheck(mineSetting.x,mineSetting.y,mineSetting.mineNumber)){
                    illegalIndicator++;
                    continue;
                }

                if(integer>MineUtil.RandomLimit){
                    illegalIndicator++;
                    continue;
                }

                //random 会覆盖额外的下注
                int[][] randomPosition = MineFactory.randomMine(mineSetting.x, mineSetting.y, integer);
                mineUtil.getData(event.getSender()).addBet(MineUtil.dataConvert(randomPosition,mineSetting.x,mineSetting.y));

                randomCount += integer;

                continue;

            }

            String[] position = element.split(",|，");

            if(position.length!=2){
                illegalIndicator++;
                continue;
            }

            int x = -1;
            int y = -1;

            try{
                x = Integer.parseInt(position[0]);
                y = Integer.parseInt(position[1]);
            } catch(Exception e){
                e.printStackTrace();
            }

            if(x!=-1&&y!=-1){

                boolean hasRepeatedValue = false;

                for(MineData md:positions){
                    if(md.x==x&&md.y==y){
                        hasRepeatedValue=true;
                        break;
                    }
                }

                for(MineData md:mineUtil.getData(event.getSender()).betList){
                    if(md.x==x&&md.y==y){
                        hasRepeatedValue=true;
                        break;
                    }
                }

                if(!hasRepeatedValue){
                    positions.add(new MineData(x,y));
                } else {
                    repeatedValue++;
                }

            } else {
                illegalIndicator++;
            }

        }

        MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event);

        if(positions.size()==0 && randomCount == 0 &&repeatedValue>0){
            mcb.append("未收到任何有效下注。请仔细阅读说明书。");
            mcb.append("\n存在").append(String.valueOf(repeatedValue)).append("处重复下注。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(positions.size()==0 && illegalIndicator==0 && randomCount == 0){
            mcb.append("未收到任何有效下注。请仔细阅读说明书。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(positions.size()==0 && randomCount==0){
            mcb.append("未收到任何有效下注，").append("存在").append(String.valueOf(illegalIndicator)).append("处指示器使用错误，请仔细阅读说明书。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if(mineUtil.getData(event.getSender())!=null) Objects.requireNonNull(mineUtil.getData(event.getSender())).addBet(positions);

        mcb.append("已收到如下下注：\n");
        StringBuilder sb = new StringBuilder();

        if(positions.size()>0) {
            for (MineData data : positions) {
                sb.append("[").append(data.x).append(",").append(data.y).append("]").append(" ");
            }

            mcb.append(sb.toString().trim());

        }

        if(randomCount>0){
            if(positions.size()>0) mcb.append("\n");
            mcb.append("随机格子").append("×").append(String.valueOf(randomCount));
        }

        mcb.append("\n").append("本局当前的倍率为×").append(String.valueOf(MineUtil.calculateOdd(event.getSubject())));

        if(illegalIndicator>0) {
            mcb.append("\n存在").append(String.valueOf(illegalIndicator)).append("处指示器使用错误，请仔细阅读说明书。");
        }

        if(repeatedValue>0) {
            mcb.append("\n存在").append(String.valueOf(repeatedValue)).append("处重复下注。");
        }

        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    @Override
    public void bet(MessageEvent event, String message) {
        if(!matchBet(message)) return;

        if(isInFunctionList.contains(event.getSubject())) return;

        if(!startBetList.contains(event.getSubject())&&!isInBetList.contains(event.getSubject())) return;

        if(!GeneralMonteCarloUtil.checkBet(event,message,mineUtil.getBet(event.getSender(),event.getSubject()))) return;

        if(startBetList.contains(event.getSubject())){
            event.getSubject().sendMessage(MineUtil.StartBetNotice);
            executorService.schedule(new EndBet(event.getSubject()), MineUtil.GapTime, TimeUnit.SECONDS);
            startBetList.remove(event.getSubject());
            isInBetList.add(event.getSubject());
        }

        if(!mineUtil.senderIsInGamingProcess(event)) {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您已经下注").append(String.valueOf(bet)).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());
            data.add(new MineUserData(event,bet));
            startBetList.remove(event.getSubject());

        } else {

            int bet = Objects.requireNonNull(GeneralMonteCarloUtil.getBet(message));
            mineUtil.addBet(event.getSender(),bet);
            MessageChainBuilder mcb = GeneralMonteCarloUtil.mcbProcessor(event).append("您总共下注").append(String.valueOf(mineUtil.getBet(event.getSender(),event.getSubject()))).append("南瓜比索。");
            event.getSubject().sendMessage(mcb.asMessageChain());

        }
    }

    @Override
    public boolean matchStart(String message) {
        return message.toLowerCase().contains("/minesweeper") || message.contains("扫雷");
    }

    @Override
    public boolean matchBet(String message) {
        return message.toLowerCase().startsWith("/bet") || message.startsWith("下注");
    }

    @Override
    public boolean matchFunction(String message) {
        for(String keywords: MineUtil.functionKeyWords){
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
        return mineUtil.subjectIsInGamingProcess(event.getSubject())||mineUtil.hasStarted(event.getSubject());
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
                mineUtil.deleteAllSubject(subject);
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
            subject.sendMessage(MineUtil.EndBetNotice + MineUtil.StartOperateNotice);
            executorService.schedule(new EndFunction(subject), MineUtil.GapTime,TimeUnit.SECONDS);
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

            int[][] resultMine = MineFactory.randomMine(
                    mines.get(subject).x,
                    mines.get(subject).y,
                    mines.get(subject).mineNumber);

            List<MineData> resultList = MineUtil.dataConvert(resultMine,
                    mines.get(subject).x,
                    mines.get(subject).y);

            List<MineData> userList = MineUtil.getUserBetList(subject);

            double times = MineUtil.calculateOdd(subject);

            MineSetting mineSetting = mines.get(subject);

            MinesweeperFace face = MinesweeperFace.Cool;

            boolean hasExploded = MineUtil.hasExploded(resultList,userList);

            if(hasExploded) face = MinesweeperFace.Dead;

            BufferedImage bi = MinesweeperImage.assembleStructure(
                    mineSetting.x,
                    mineSetting.y,
                    MineFactory.intToArray(mineSetting.mineNumber),
                    MineFactory.doubleToArray(mineSetting.odd),
                    face);

            MinesweeperImage.addPool(bi,
                    mineSetting.x,
                    mineSetting.y,
                    MinesweeperPoolType.TouchedPool,
                    resultMine,
                    MineUtil.dataConvert(
                            userList,
                            mineSetting.x,
                            mineSetting.y)
            );

            BufferedImage result = MinesweeperImageUtil.drawPureGreenBackground(bi);

            MessageChainBuilder mcb = new MessageChainBuilder().append(MineUtil.EndGameNotice).append("\n");

            for(MineUserData mud:mineUtil.getUserList(subject)){

                if(mud.betList.size()==0) continue;

                if(mud.isGroup()){
                    mcb.append("\n").append(new At(mud.sender.getId()));
                } else {
                    mcb.append("\n您");
                }

                int money = 0;
                if(!hasExploded) money = (int) (mud.bet * times);

                if(money>mud.bet) {
                    PumpkinPesoWindow.addMoney(mud.sender.getId(),money-mud.bet);
                } else {
                    PumpkinPesoWindow.minusMoney(mud.sender.getId(),money-mud.bet);
                }

                mcb.append("获得了").append(String.valueOf(money)).append("南瓜比索。");

            }

            mcb.append("\n总倍率为×");

            if(hasExploded) {
                mcb.append(String.valueOf(0));
            } else {
                mcb.append(String.valueOf(MineUtil.calculateOdd(subject)));
            }

            mcb.append("\n").append(Contact.uploadImage(subject,ImageSender.getBufferedImageAsSource(result)));

            subject.sendMessage(mcb.asMessageChain());
            mineUtil.clear(subject);

        }
    }
}
