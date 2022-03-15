package lielietea.mirai.plugin.core.game.montecarlo.blackjack;

import lielietea.mirai.plugin.administration.statistics.GameCenterCount;
import lielietea.mirai.plugin.core.bank.PumpkinPesoWindow;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.data.BlackJackData;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.data.BlackJackPlayer;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.enums.BlackJackPhase;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BlackJack extends BlackJackUtils {

    static final String BlackJackRules = "里格斯公司邀请您参与本局 Blackjack，请在60秒之内输入 /bet+数字 参与游戏。";
    static final String BlackJackStops = "本局 Blackjack 已经取消。";
    static final String NotRightBetNumber = "/bet 指令不正确，请重新再尝试";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。";
    static final String BustNotice = "您爆牌了。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";

    static final String BLACKJACK_INTRO_PATH = "/pics/casino/blackjack.png";
    static final int GAP_SECONDS = 60;

    public List<BlackJackData> globalGroupData = new ArrayList<>();
    public List<BlackJackData> globalFriendData = new ArrayList<>();

    public Map<Long, Timer> groupBlackjackCancelTimer = new HashMap<>();
    public Map<Long, Timer> friendBlackjackCancelTimer = new HashMap<>();
    public Map<Long, Timer> groupEndBetTimer = new HashMap<>();
    public Map<Long, Timer> groupEndOperationTimer = new HashMap<>();
    public Map<Long, Timer> friendEndOperationTimer = new HashMap<>();

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    List<Long> isInBetProcess = new ArrayList<>();
    Map<Date, Long> GroupResetMark = new HashMap<>();
    Map<Date, Long> FriendResetMark = new HashMap<>();

    BlackJack() {
    }

    private static final BlackJack INSTANCE;

    static {
        INSTANCE = new BlackJack();

    }

    public static BlackJack getINSTANCE() {
        return INSTANCE;
    }

    //更改列表里面的状态
    public static void changePhase(MessageEvent event, BlackJackPhase bjp) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event, getGlobalData(event))).setPhase(bjp);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event, getGlobalData(event))).setPhase(bjp);
        }
    }

    //更改赌注的金额
    public static void changeBet(MessageEvent event, int additionalBet) {
        if (event.getClass().equals(GroupMessageEvent.class)) {
            int overwriteBet = additionalBet + getGlobalData(event).get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
            getINSTANCE().globalGroupData.get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBet(overwriteBet);
        } else {
            int overwriteBet = additionalBet + getGlobalData(event).get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
            getINSTANCE().globalFriendData.get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBet(overwriteBet);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //对话框中输入/blackjack或者二十一点
    public static void checkBlackJack(MessageEvent event) {
        if (!isBlackJack(event)) return;
        if (isInTheList(event, getGlobalData(event))) return;



        //全局取消标记
        Date gameStartTime = new Date();
        if (isGroupMessage(event)) {
            while (getINSTANCE().GroupResetMark.containsKey(gameStartTime)) {
                gameStartTime.setTime(gameStartTime.getTime() - 1);
            }
            getINSTANCE().GroupResetMark.put(gameStartTime, event.getSubject().getId());
        } else {
            while (getINSTANCE().FriendResetMark.containsKey(gameStartTime)) {
                gameStartTime.setTime(gameStartTime.getTime() - 1);
            }
            getINSTANCE().FriendResetMark.put(gameStartTime, event.getSubject().getId());
        }

        //3.5个间隔时间之后，取消标记
        executor.schedule(new CancelMarks(event, gameStartTime), (long) (GAP_SECONDS * 3.5), TimeUnit.SECONDS);

        InputStream img = BlackJack.class.getResourceAsStream(BLACKJACK_INTRO_PATH);
        assert img != null;
        GameCenterCount.count(GameCenterCount.Functions.BlackjackStart);

        //前置标记取消
        cancelMark(event);
        event.getSubject().sendMessage(new MessageChainBuilder().append(BlackJackRules).append("\n\n").append(Contact.uploadImage(event.getSubject(), img)).asMessageChain());
        try {
            img.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cancelInSixtySeconds(event);

        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.add(new BlackJackData(event.getSubject().getId()));
        } else {
            getINSTANCE().globalFriendData.add(new BlackJackData(event.getSubject().getId()));
        }
    }

    //3.5个GAP_TIME之后取消所有标记的Runnable
    static class CancelMarks implements Runnable {
        MessageEvent event;
        Date gameStartTime;

        CancelMarks(MessageEvent event, Date gameStartTime) {
            this.event = event;
            this.gameStartTime = gameStartTime;
        }

        @Override
        public void run() {
            if (isGroupMessage(event)) {
                if (getINSTANCE().GroupResetMark.containsKey(gameStartTime)) {
                    getINSTANCE().globalGroupData.remove((int) indexInTheList(event));
                    getINSTANCE().GroupResetMark.remove(gameStartTime);
                    getINSTANCE().isInBetProcess.remove(event.getSubject().getId());
                }
            } else {
                if (getINSTANCE().FriendResetMark.containsKey(gameStartTime)) {
                    getINSTANCE().globalFriendData.remove((int) indexInTheList(event));
                    getINSTANCE().FriendResetMark.remove(gameStartTime);
                }
            }
        }
    }

    static void cancelMark(MessageEvent event) {
        if (isGroupMessage(event)) {
            for (Date date : getINSTANCE().GroupResetMark.keySet()) {
                if (getINSTANCE().GroupResetMark.get(date) == event.getSubject().getId()) {
                    getINSTANCE().GroupResetMark.remove(date);
                    break;
                }
            }
            try {
                getINSTANCE().globalGroupData.remove((int) indexInTheList(event));
                getINSTANCE().isInBetProcess.remove(event.getSubject().getId());
            } catch(Exception e){
                e.printStackTrace();
            }

        } else {
            for (Date date : getINSTANCE().FriendResetMark.keySet()) {
                if (getINSTANCE().FriendResetMark.get(date) == event.getSubject().getId()) {
                    getINSTANCE().FriendResetMark.remove(date);
                    break;
                }
            }
            try {
                getINSTANCE().globalFriendData.remove((int) indexInTheList(event));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //60秒之内如果没有进入下一阶段就自动取消
    public static void cancelInSixtySeconds(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().groupBlackjackCancelTimer.put(event.getSubject().getId(), new Timer(true));
            getINSTANCE().groupBlackjackCancelTimer.get(event.getSubject().getId()).schedule(new TimerTask() {
                @Override
                public void run() {
                    Integer index = indexInTheList(event);
                    if (index == null) {
                        event.getSubject().sendMessage(BlackJackStops);
                        return;
                    }

                    if (getINSTANCE().globalGroupData.get(index).getPhase().equals(BlackJackPhase.Callin)) {
                        event.getSubject().sendMessage(BlackJackStops);
                        getINSTANCE().globalGroupData.remove(getGlobalData(event).get(index));
                    }
                    getINSTANCE().groupBlackjackCancelTimer.get(event.getSubject().getId()).cancel();
                    getINSTANCE().groupBlackjackCancelTimer.remove(event.getSubject().getId());
                }
            }, 60 * 1000);

        } else {
            getINSTANCE().friendBlackjackCancelTimer.put(event.getSubject().getId(), new Timer(true));
            getINSTANCE().friendBlackjackCancelTimer.get(event.getSubject().getId()).schedule(new TimerTask() {
                @Override
                public void run() {
                    Integer index = indexInTheList(event);
                    if (index == null) {
                        event.getSubject().sendMessage(BlackJackStops);
                        return;
                    }

                    if (getINSTANCE().globalFriendData.get(index).getPhase().equals(BlackJackPhase.Callin)) {
                        event.getSubject().sendMessage(BlackJackStops);
                        getINSTANCE().globalFriendData.remove(getGlobalData(event).get(index));
                    }
                    getINSTANCE().friendBlackjackCancelTimer.get(event.getSubject().getId()).cancel();
                    getINSTANCE().friendBlackjackCancelTimer.remove(event.getSubject().getId());
                }
            }, 60 * 1000);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //对话框中输入/bet或者下注
    public static void checkBet(MessageEvent event) {
        System.out.println("Check Bet Activity");
        if (!isBet(event)) return;
        System.out.println("Is Bet Event");
        if (!isInGamingProcess(event)) return;
        System.out.println("Is in gaming process");
        if (getGlobalData(event).get(indexInTheList(event)).getPhase() == BlackJackPhase.Operation) return;
        System.out.println("count");
        GameCenterCount.count(GameCenterCount.Functions.BlackjackBet);

        //判定数值是否正确
        Integer bet = null;
        try {
            bet = getBet(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bet == null) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if (bet <= 0) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        if (bet > 999999) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        //判定账户里是否有钱
        if (!hasEnoughMoney(event, bet)) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(YouDontHaveEnoughMoney);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return;
        }

        //进行判定是Friend还是Group
        if (isGroupMessage(event)) {
            //将当前的Phase改为下注阶段Bet
            changePhase(event, BlackJackPhase.Bet);
            //扣钱
            PumpkinPesoWindow.minusMoney(event.getSender().getId(), bet);
            //如果已经有了则追加写入
            if (playerIsInTheList(event)) {
                changeBet(event, bet);
                int totalBet = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
                MessageChainBuilder mcb = mcbProcessor(event);
                event.getSubject().sendMessage(mcb.append("共收到下注").append(String.valueOf(totalBet)).append("南瓜比索。").asMessageChain());
            } else {
                //写入赌注
                if (indexOfTheBookMaker(event) == null) {
                    addBookmaker(event);
                }
                addNewPlayer(event, bet);
                MessageChainBuilder mcb = mcbProcessor(event);
                event.getSubject().sendMessage(mcb.append("已收到下注").append(String.valueOf(bet)).append("南瓜比索。").asMessageChain());
            }
            //如果没有第一次进入过下注阶段，那么给List加一个flag，设置定时关闭任务
            //下注阶段在第一个人下注后60秒关闭
            if (!getINSTANCE().isInBetProcess.contains(event.getSubject().getId())) {
                event.getSubject().sendMessage(StartBetNotice);
                getINSTANCE().isInBetProcess.add(event.getSubject().getId());
                //关闭blackjack的这个timer
                getINSTANCE().groupBlackjackCancelTimer.get(event.getSubject().getId()).cancel();
                //召唤庄家
                if (indexOfTheBookMaker(event) == null) {
                    addBookmaker(event);
                }
                //定时任务
                endBetInSixtySeconds(event);
            }
            //FriendMessageEvent 开始
        } else {
            //关闭Timer
            getINSTANCE().friendBlackjackCancelTimer.get(event.getSubject().getId()).cancel();
            //将当前的Phase改为下注阶段Bet
            changePhase(event, BlackJackPhase.Bet);
            //扣钱
            PumpkinPesoWindow.minusMoney(event.getSender().getId(), bet);
            //召唤庄家
            addBookmaker(event);
            //写入赌注
            addNewPlayer(event, bet);
            //进入发牌操作
            changePhase(event, BlackJackPhase.Operation);
            cardShuffle(event);
            dealCards(event);
            showTheCards(event);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //下注阶段的定时任务
    public static void endBetInSixtySeconds(MessageEvent event) {
        getINSTANCE().groupEndBetTimer.put(event.getSubject().getId(), new Timer(true));
        getINSTANCE().groupEndBetTimer.get(event.getSubject().getId()).schedule(new TimerTask() {
            @Override
            public void run() {
                endBetActivity(event);
            }
        }, 60 * 1000);
    }

    //定时任务里的任务

    public static void endBetActivity(MessageEvent event) {
        event.getSubject().sendMessage(EndBetNotice);
        //删除该flag
        getINSTANCE().isInBetProcess.remove(event.getSubject().getId());
        //更改状态
        changePhase(event, BlackJackPhase.Operation);
        //进入发牌操作
        cardShuffle(event);
        System.out.println("洗完牌了");
        dealCards(event);
        System.out.println("发完牌了");
        showTheCards(event);
        getINSTANCE().groupEndBetTimer.get(event.getSubject().getId()).cancel();
        getINSTANCE().groupEndBetTimer.remove(event.getSubject().getId());
    }

    //查看用户在列表里第几个
    public static Integer indexOfThePlayer(MessageEvent event) {
        return indexOfThePlayer(event, event.getSender().getId());
    }

    //查看用户在列表里第几个
    public static Integer indexOfThePlayer(MessageEvent event, long ID) {
        return indexOfThePlayer(getGlobalData(event).get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList(), ID);
    }

    //查看庄家在列表里第几个
    public static Integer indexOfTheBookMaker(MessageEvent event) {
        return indexOfThePlayer(getGlobalData(event).get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList(), 0);
    }


    //查看用户是否在该群的列表里
    public static boolean playerIsInTheList(MessageEvent event) {
        return playerIsInTheList(getGlobalData(event).get(indexInTheList(event, getGlobalData(event))).getBlackJackPlayerList(), event.getSender().getId());

    }

    //查看全局列表里是几号
    public static Integer indexInTheList(MessageEvent event) {
        return indexInTheList(event, getGlobalData(event));
    }

    //添加新玩家
    public static void addNewPlayer(MessageEvent event, int bet) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).addBlackJackPlayerList(new BlackJackPlayer(event.getSender().getId(), bet));
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).addBlackJackPlayerList(new BlackJackPlayer(event.getSender().getId(), bet));
        }

    }

    //添加庄家
    public static void addBookmaker(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).addBlackJackPlayerList(new BlackJackPlayer(true));
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).addBlackJackPlayerList(new BlackJackPlayer(true));
        }
    }

    //发牌操作
    public static void cardShuffle(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).setCardPile(createPokerPile());
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).setCardPile(createPokerPile());
        }
    }

    //翻牌操作
    public static void dealCards(MessageEvent event) {
        int playerNumber = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().size();
        for (int index = 0; index < playerNumber; index++) {
            List<Integer> cardList = new ArrayList<>();
            //抽两张牌，一张是当张，一张是当张+人数（包括庄家）
            cardList.add(getGlobalData(event).get(indexInTheList(event)).getCardPile().get(index));
            cardList.add(getGlobalData(event).get(indexInTheList(event)).getCardPile().get(index + playerNumber));
            //塞牌
            if (isGroupMessage(event)) {
                getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCards(cardList);
            } else {
                getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCards(cardList);
            }
        }
        //设置已经被抽出来的张数
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).setCardnumber(playerNumber * 2);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).setCardnumber(playerNumber * 2);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //告知玩家牌面情况
    public static void showTheCards(MessageEvent event) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append("抽牌情况如下：");
        if (isGroupMessage(event)) {
            for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
                mcb.append(groupMCBBuilder(bjp, bjp.getID()));
            }

            //FriendMessageEvent
        } else {
            for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
                mcb.append(friendMCBBuilder(bjp));
            }
        }
        event.getSubject().sendMessage(mcb.asMessageChain());
        //允许玩家操作
        allowPlayerToOperate(event);
        //设置定时任务
        if (isGroupMessage(event)) {
            getINSTANCE().groupEndOperationTimer.put(event.getSubject().getId(), new Timer(true));
            getINSTANCE().groupEndOperationTimer.get(event.getSubject().getId()).schedule(new TimerTask() {
                @Override
                public void run() {
                    foldEveryoneInSixtySeconds(event);
                }
            }, 60 * 1000);
        } else {
            getINSTANCE().friendEndOperationTimer.put(event.getSubject().getId(), new Timer(true));
            getINSTANCE().friendEndOperationTimer.get(event.getSubject().getId()).schedule(new TimerTask() {
                @Override
                public void run() {
                    foldEveryoneInSixtySeconds(event);
                }
            }, 60 * 1000);
        }

    }

    //showTheCards里面用到的群界面返回MessageChainBuilder
    public static MessageChain groupMCBBuilder(BlackJackPlayer bjp, long ID) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (bjp.isBookmaker()) {
            mcb.append("\n庄家的牌是：\n");
            mcb.append(" ").append(getPoker(bjp.getCards().get(0))).append(" 暗牌");
        } else {
            mcb.append("\n\n").append((new At(ID))).append(" 的牌是：\n");
            for (Integer card : bjp.getCards()) {
                mcb.append(" ").append(getPoker(card));
            }
        }
        return mcb.asMessageChain();
    }

    //showTheCards里面用到的用户界面返回MessageChainBuilder
    public static MessageChain friendMCBBuilder(BlackJackPlayer bjp) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (bjp.getID() == 0) {
            mcb.append("\n\n").append("庄家的牌是：");
            mcb.append("\n").append(getPoker(bjp.getCards().get(0))).append(" 暗牌");
        } else {
            mcb.append("\n\n").append("你的牌是：\n");
            for (Integer card : bjp.getCards()) {
                mcb.append(" ").append(getPoker(card));
            }
        }
        return mcb.asMessageChain();
    }

    //解除禁止玩家操作
    public static void allowPlayerToOperate(MessageEvent event) {
        int index = 0;
        for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
            if (!bjp.isBookmaker()) {
                if (isGroupMessage(event)) {
                    getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCanOperate(true);
                } else {
                    getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCanOperate(true);
                }
            }
            index += 1;
        }
        event.getSubject().sendMessage(StartOperateNotice);
    }

    //定时任务：60秒内把所有玩家全部fold掉，并强制进入结算模式
    public static void foldEveryoneInSixtySeconds(MessageEvent event) {
        int index = 0;
        for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
            if (!bjp.isBookmaker()) {
                if (isGroupMessage(event)) {
                    getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCanOperate(false);
                } else {
                    getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(index).setCanOperate(false);
                }
            }
            index += 1;
        }
        endOperationTimer(event);
        resultCalculator(event);
    }

    public static void endOperationTimer(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().groupEndOperationTimer.get(event.getSubject().getId()).cancel();
            getINSTANCE().groupEndOperationTimer.remove(event.getSubject().getId());
        } else {
            getINSTANCE().friendEndOperationTimer.get(event.getSubject().getId()).cancel();
            getINSTANCE().friendEndOperationTimer.remove(event.getSubject().getId());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //开始玩家操作
    public static void playerOperation(MessageEvent event) {
        if (bjOperation(event) == null) return;
        if (!isInGamingProcess(event)) return;
        if (!operationAvailabilityCheck(event)) return;

        //主操作
        startOperation(event);
        //操作完之后判定是否都fold
        if (!haveAllFolded(event)) return;
        endOperationTimer(event);
        //结算了
        resultCalculator(event);
    }

    //playOperation里面使用的Switch
    public static void startOperation(MessageEvent event) {
        GameCenterCount.count(GameCenterCount.Functions.BlackjackOperations);
        switch (Objects.requireNonNull(bjOperation(event))) {
            case Assurance:
                assurance(event);
                break;
            case Deal:
                deal(event);
                break;
            case Double:
                doubleBet(event);
                break;
            case Fold:
                fold(event);
                break;
            case Pair:
                pair(event);
                break;
            case Split:
                split(event);
                break;
            case Surrender:
                surrender(event);
                break;
        }
    }

    //判定是否全员fold
    public static boolean haveAllFolded(MessageEvent event) {
        for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
            if (bjp.isBookmaker()) continue;
            if (bjp.isCanOperate()) return false;
        }
        return true;

    }

    //返回是否可以操作
    public static boolean operationAvailabilityCheck(MessageEvent event) {
        boolean containsUser = false;
        for (BlackJackData bjd : getGlobalData(event)) {
            for (BlackJackPlayer bjp : bjd.getBlackJackPlayerList()) {
                if (event.getSender().getId() == bjp.getID()) {
                    containsUser = true;
                    break;
                }
            }
        }
        if (!containsUser) return false;
        return (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).isCanOperate());
    }

    //保险
    public static void assurance(MessageEvent event) {
        if (!canBuyAssurance(event)) {
            event.getSubject().sendMessage(mcbProcessor(event).append("目前的牌局无法购买保险。").asMessageChain());
            return;
        }
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasAssurance(true);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasAssurance(true);
        }
        event.getSubject().sendMessage(mcbProcessor(event).append("您已经购买保险。").asMessageChain());

    }

    //要牌
    public static void deal(MessageEvent event) {
        //塞牌进去，增加卡牌数量，判定是否爆牌，爆了就不能operate，
        getCardSendNotice(event, 1);
        bustThatMthrfckr(event);
    }

    //双倍下注
    public static void doubleBet(MessageEvent event) {
        //只有没双倍才能双倍
        if (!canDouble(event)) {
            event.getSubject().sendMessage(mcbProcessor(event).append("您的牌无法双倍下注。").asMessageChain());
            return;
        }
        int bet = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
        if (!hasEnoughMoney(event, bet)) {
            //很遗憾地通知您 您没有钱
            event.getSubject().sendMessage(mcbProcessor(event).append(YouDontHaveEnoughMoney).asMessageChain());
            return;
        }
        PumpkinPesoWindow.minusMoney(event.getSender().getId(), bet);
        getCardSendNotice(event, 1);
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBet(bet * 2);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBet(bet * 2);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        }
        event.getSubject().sendMessage(mcbProcessor(event).append("您已经双倍下注。").asMessageChain());
    }

    //停牌
    public static void fold(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        }
        event.getSubject().sendMessage(mcbProcessor(event).append("您已经停牌。").asMessageChain());
    }

    //下注对子
    public static void pair(MessageEvent event) {
        if (!canBetPair(event)) {
            event.getSubject().sendMessage(mcbProcessor(event).append("无法重复下注对子。").asMessageChain());
            return;
        }
        //下注对子扣钱
        int bet = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
        //如果有钱就能扣款了
        if (!hasEnoughMoney(event, bet)) {
            //很遗憾地通知您 您没有钱
            event.getSubject().sendMessage(mcbProcessor(event).append(YouDontHaveEnoughMoney).asMessageChain());
            return;
        }
        PumpkinPesoWindow.minusMoney(event.getSender().getId(), bet);
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBetPair(true);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setBetPair(true);
        }
        event.getSubject().sendMessage(mcbProcessor(event).append("您已经下注对子。").asMessageChain());
    }

    //分牌
    public static void split(MessageEvent event) {
        if (!canSplitTheCards(event)) return;
        //扣款！
        int bet = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getBet();
        if (!hasEnoughMoney(event, bet)) {
            //很遗憾地通知您 您没有钱
            event.getSubject().sendMessage(mcbProcessor(event).append(YouDontHaveEnoughMoney).asMessageChain());
            return;
        }
        PumpkinPesoWindow.minusMoney(event.getSender().getId(), bet);
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasSplit(true);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasSplit(true);
        }
        //获得首张牌
        Integer firstCard;
        if (isGroupMessage(event)) {
            firstCard = getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(0);
        } else {
            firstCard = getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(0);
        }
        List<Integer> firstPile = new ArrayList<>();
        List<Integer> secondPile = new ArrayList<>();
        firstPile.add(firstCard);
        secondPile.add(firstCard);
        //抽第一组
        while (cardPointCalculator(firstPile) < 17) {
            firstPile.add(getCard(event));
        }
        //第一个牌组抽完了，增加一个null作为分割
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().add(null);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().add(null);
        }
        //抽第二组
        while (cardPointCalculator(secondPile) < 17) {
            secondPile.add(getCard(event));
        }
        splitGetCardSendNotice(event);
    }

    //投降，改变flag且停牌
    public static void surrender(MessageEvent event) {
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasSurrendered(true);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasSurrendered(true);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        }
        event.getSubject().sendMessage(mcbProcessor(event).append("您投降了，将会返还您一半的赌注。").asMessageChain());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //能否买保险
    public static boolean canBuyAssurance(MessageEvent event) {
        return getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards().get(0) % 13 == 1;
    }

    //能否分牌
    public static boolean canSplitTheCards(MessageEvent event) {
        List<Integer> cardList = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards();
        return (cardList.size() == 2) && cardPointCalculator(cardList.get(0)) == (cardPointCalculator(cardList.get(1)));
    }

    //能否双倍下注
    public static boolean canDouble(MessageEvent event) {
        List<Integer> cardList = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards();
        if ((cardList.size() != 2)) return false;
        int cardPoint = cardPointCalculator(cardList);
        boolean isAPlusTen = false;
        if (cardPoint == 21) {
            for (Integer card : cardList) {
                if (card % 13 == 1) {
                    isAPlusTen = true;
                    break;
                }
            }
        }
        if (!isAPlusTen) {
            if (cardPoint != 11) return false;
        }
        return !getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).isDouble();
    }

    //能否下注对子
    public static boolean canBetPair(MessageEvent event) {
        return !getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).isBetPair();
    }

    //计算点数
    public static int cardPointCalculator(List<Integer> cardList) {
        int totalPoints = 0;
        List<Integer> actualCardList = new ArrayList<>();
        for (Integer card : cardList) {
            int actualCard = card % 13;
            if (actualCard > 10) actualCard = 10;
            if (actualCard == 0) actualCard = 10;
            if (actualCard == 1) actualCard = 11;
            actualCardList.add(actualCard);
            totalPoints += actualCard;
            if ((totalPoints > 21) && actualCardList.contains(11)) {
                actualCardList.remove(Integer.valueOf(11));
                actualCardList.add(1);
                totalPoints += -10;
            }
        }

        System.out.println("点数计算结果是" + totalPoints);
        return totalPoints;
    }

    //计算单张牌的点数，A算11
    public static int cardPointCalculator(Integer card) {
        int actualCard = card % 13;
        if (actualCard > 10) actualCard = 10;
        if (actualCard == 0) actualCard = 10;
        if (actualCard == 1) actualCard = 11;
        return actualCard;
    }

    //判定是否爆牌了
    public static boolean hasBusted(MessageEvent event) {
        List<Integer> cardList = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards();
        return cardPointCalculator(cardList) > 21;
    }

    public static boolean hasBusted(int points) {
        return points > 21;
    }

    //爆牌操作
    public static void bustThatMthrfckr(MessageEvent event) {
        if (!hasBusted(event)) return;
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasBusted(true);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setHasBusted(true);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).setCanOperate(false);
        }
        MessageChainBuilder mcb = mcbProcessor(event);
        event.getSubject().sendMessage(mcb.append(BustNotice).asMessageChain());
    }

    //抽一张卡
    public static int getCard(MessageEvent event) {
        int cardNumber = getGlobalData(event).get(indexInTheList(event)).getCardnumber();
        int card = getGlobalData(event).get(indexInTheList(event)).getCardPile().get(cardNumber);
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).addCards(card);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).setCardnumber(cardNumber + 1);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).addCards(card);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).setCardnumber(cardNumber + 1);
        }
        return card;
    }

    //庄家抽一张卡
    public static int bookMakerGetCard(MessageEvent event) {
        int cardNumber = getGlobalData(event).get(indexInTheList(event)).getCardnumber();
        int card = getGlobalData(event).get(indexInTheList(event)).getCardPile().get(cardNumber);
        if (isGroupMessage(event)) {
            getINSTANCE().globalGroupData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).addCards(card);
            getINSTANCE().globalGroupData.get(indexInTheList(event)).setCardnumber(cardNumber + 1);
        } else {
            getINSTANCE().globalFriendData.get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).addCards(card);
            getINSTANCE().globalFriendData.get(indexInTheList(event)).setCardnumber(cardNumber + 1);
        }
        return card;
    }

    //抽卡操作 先抽卡，给牌堆+1，然后发送消息
    public static void getCardSendNotice(MessageEvent event, int num) {
        MessageChainBuilder mcb = mcbProcessor(event);
        mcb.append("您抽到的牌是：");
        for (int i = 0; i < num; i++) {
            mcb.append("\n").append(getPoker(getCard(event)));
        }
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    //Split抽卡操作 先抽卡，给牌堆+1，然后发送消息
    public static void splitGetCardSendNotice(MessageEvent event) {
        MessageChainBuilder mcb = mcbProcessor(event);
        mcb.append("您的原始牌为：\n").append(getPoker(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(0))).append(" ")
                .append(getPoker(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(0)));
        mcb.append("\n您两个牌堆抽到的牌分别是：\n牌堆I：");
        int nullLocation = 0;
        int cardListSize = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().size();
        for (Integer card : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards()) {
            if (card == null) {
                break;
            }
            nullLocation++;
        }
        //输出第一组牌
        for (int index = 2; index < nullLocation; index++) {
            mcb.append(" ").append(getPoker(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(index)));
        }
        mcb.append("\n牌堆II：");
        for (int index = nullLocation + 1; index < cardListSize; index++) {
            mcb.append(" ").append(getPoker(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event)).getCards().get(index)));
        }
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //结算
    public static void resultCalculator(MessageEvent event) {
        try {
            System.out.println("进入resultCalculator");
            //庄家先操作
            bookmakerDoesTheFinalMove(event);
            System.out.println("庄家动完");
            //计算分值
            Map<Long, Double> resultMap = getFinalPoints(event);
            System.out.println("计算分值");
            //返回赌款
            for (Long ID : resultMap.keySet()) {
                PumpkinPesoWindow.addMoney(ID, (int) Math.round(resultMap.get(ID)));
            }
            System.out.println("返回赌款");
            //赌场进出帐
            casinoHasItsFinalLaugh(resultMap);
            System.out.println("赌场进出帐");
            //通知玩家
            sendFinalNotice(event, resultMap);
            System.out.println("通知完毕");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //删除这副牌
            if (isGroupMessage(event)) {
                try {
                    getINSTANCE().globalGroupData.remove((int) indexInTheList(event));
                    getINSTANCE().isInBetProcess.remove(event.getSubject().getId());
                } finally {
                    for (Date date : getINSTANCE().GroupResetMark.keySet()) {
                        if (getINSTANCE().GroupResetMark.get(date) == event.getSubject().getId()) {
                            getINSTANCE().GroupResetMark.remove(date);
                            break;
                        }
                    }
                }
            } else {
                try {
                    getINSTANCE().globalFriendData.remove((int) indexInTheList(event));
                } finally {
                    for (Date date : getINSTANCE().FriendResetMark.keySet()) {
                        if (getINSTANCE().FriendResetMark.get(date) == event.getSubject().getId()) {
                            getINSTANCE().FriendResetMark.remove(date);
                            break;
                        }
                    }
                }
            }
        }
    }

    //查看庄家的牌有没有超过17点
    public static boolean bookmakerNeedsToGetMoreCards(MessageEvent event) {
        return cardPointCalculator(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards()) < 17;
    }

    //庄家的最后操作
    public static void bookmakerDoesTheFinalMove(MessageEvent event) {
        //只要没到十七点就得继续开牌
        List<Integer> cardList = new ArrayList<>(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards());
        while (bookmakerNeedsToGetMoreCards(event)) {
            cardList.add(bookMakerGetCard(event));
        }
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append("庄家开的牌组是：\n");

        for (Integer card : cardList) {
            mcb.append(" ").append(getPoker(card));
        }
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    public static Map<Long, Double> getFinalPoints(MessageEvent event) {
        Map<Long, Double> resultMap = new HashMap<>();
        for (BlackJackPlayer bjp : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList()) {
            if (bjp.isBookmaker()) continue;
            int bet = bjp.getBet();
            resultMap.put(bjp.getID(), bet * calculateGeneralPoint(event, bjp.getID()));
        }
        return resultMap;
    }

    //不算保险、对子
    public static double calculateNormalPoint(MessageEvent event, long ID) {
        //投降
        if (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasSurrendered())
            return 0.5;
        //分牌
        if (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasSplit())
            return calculateSplitPoint(event, ID);
        //爆了
        if (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasBusted())
            return 0;
        //计算庄家点数
        int bookmakersPoints = cardPointCalculator(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards());
        //庄家爆了
        if (hasBusted(bookmakersPoints)) return 2;
        //计算玩家点数
        int playersPoints = cardPointCalculator(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards());
        return calculateBigOrSmall(bookmakersPoints, playersPoints);
    }

    public static int calculateSplitPoint(MessageEvent event, long ID) {
        int bookmakersPoints = cardPointCalculator(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards());
        if (bookmakersPoints > 21) bookmakersPoints = 1;
        List<Integer> playersPointsList1 = new ArrayList<>();
        List<Integer> playersPointsList2 = new ArrayList<>();
        int firstCard = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards().get(0);
        int nullLocation = 0;
        int cardListSize = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards().size();
        for (Integer card : getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards()) {
            if (card == null) break;
            nullLocation++;
        }
        //第一个牌堆
        playersPointsList1.add(firstCard);
        playersPointsList2.add(firstCard);
        for (int index = 2; index < nullLocation; index++) {
            playersPointsList1.add(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards().get(index));
        }
        //第二个牌堆
        for (int index = nullLocation + 1; index < cardListSize; index++) {
            playersPointsList2.add(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards().get(index));
        }
        int playersPoints1 = cardPointCalculator(playersPointsList1);
        int playersPoints2 = cardPointCalculator(playersPointsList2);
        if (playersPoints1 > 21) playersPoints1 = 0;
        if (playersPoints2 > 21) playersPoints2 = 0;
        return calculateBigOrSmall(bookmakersPoints, playersPoints1) + calculateBigOrSmall(bookmakersPoints, playersPoints2);
    }

    //比大小返回
    public static int calculateBigOrSmall(int bookmakersPoints, int playersPoints) {
        if (playersPoints == bookmakersPoints) return 1;
        if (playersPoints > bookmakersPoints) return 2;
        return 0;
    }

    //计算对子
    public static int calculatePairPoint(MessageEvent event, long ID) {
        if (!getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isBetPair())
            return 0;
        int card1 = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards().get(0) % 13;
        int card2 = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards().get(1) % 13;
        if (card1 == card2) return 11;
        event.getBot().login();
        return 0;
    }

    //计算保险
    public static int calculateAssurancePoint(MessageEvent event, long ID) {
        if (!getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasAssurance())
            return 0;
        int coefficient = 1;
        if (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasSplit()) {
            coefficient = 2;
        }
        int card = cardPointCalculator(getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfTheBookMaker(event)).getCards().get(1));
        if (card == 10) return coefficient;
        return 0;
    }

    //计算特殊牌型
    public static int calculateSpecialPattern(MessageEvent event, long ID) {
        List<Integer> cardList = getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).getCards();
        if (cardList.size() != 3) return 0;
        if (cardList.contains(6) && cardList.contains(7) && cardList.contains(8)) {
            return 3;
        }
        if (cardList.get(0) == 7 && cardList.get(1) == 7 && cardList.get(2) == 7) {
            return 3;
        }
        return 0;
    }

    //计算总倍率
    public static double calculateGeneralPoint(MessageEvent event, long ID) {
        if (getGlobalData(event).get(indexInTheList(event)).getBlackJackPlayerList().get(indexOfThePlayer(event, ID)).isHasAssurance()) {
            return (0.5 * (calculateNormalPoint(event, ID) + calculatePairPoint(event, ID)) + calculateAssurancePoint(event, ID) + calculateSpecialPattern(event, ID));
        }
        return (calculateNormalPoint(event, ID) + calculatePairPoint(event, ID) + calculateSpecialPattern(event, ID));
    }

    //发送最终结算
    public static void sendFinalNotice(MessageEvent event, Map<Long, Double> resultMap) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append(EndGameNotice).append("\n");
        for (long ID : resultMap.keySet()) {
            if (isGroupMessage(event)) {
                mcb.append("\n").append(new At(ID)).append(" 获得了").append(String.valueOf(resultMap.get(ID))).append("南瓜比索");
            } else {
                mcb.append("\n").append("您获得了").append(String.valueOf(resultMap.get(ID))).append("南瓜比索。");
            }
        }
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    public static void casinoHasItsFinalLaugh(Map<Long, Double> resultMap) {
        double finalAmount = 0;
        for (long ID : resultMap.keySet()) {
            finalAmount += resultMap.get(ID);
        }
        if (finalAmount > 0) {
            PumpkinPesoWindow.addMoney(0, (int) Math.round(finalAmount));
        }
        if (finalAmount < 0) PumpkinPesoWindow.minusMoneyMaybeAllIn(0, -(int) Math.round(finalAmount));
    }

    //BlackJackData应该是哪个MessageEvent下的data
    public static List<BlackJackData> getGlobalData(MessageEvent event) {
        if (isGroupMessage(event)) {
            return getINSTANCE().globalGroupData;
        }
        return getINSTANCE().globalFriendData;
    }

    //返回是否在进行游戏
    public static boolean isInGamingProcess(MessageEvent event) {
        for (BlackJackData bjd : getGlobalData(event)) {
            if (bjd.getID() == event.getSubject().getId()) return true;
        }
        return false;
    }

    //主方法
    public static void go(MessageEvent event) {
        checkBlackJack(event);
        checkBet(event);
        playerOperation(event);
        adminToolsInBlackJack(event);
    }
}
