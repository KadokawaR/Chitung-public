package lielietea.mirai.plugin.core.game.montecarlo.roulette;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lielietea.mirai.plugin.administration.statistics.GameCenterCount;
import lielietea.mirai.plugin.core.bank.PumpkinPesoWindow;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Roulette extends RouletteUtils {

    Roulette() {
    }

    private static final Roulette INSTANCE;

    static {
        INSTANCE = new Roulette();
    }

    public static Roulette getINSTANCE() {
        return INSTANCE;
    }

    public enum StatusType {
        Callin,
        PreBet,
        Bet,
        End
    }

    Map<Long, StatusType> GroupStatusMap = new HashMap<>();
    Map<Long, StatusType> FriendStatusMap = new HashMap<>();
    Table<Long, Long, Integer> GroupBet = HashBasedTable.create();
    Map<Long, Integer> FriendBet = new HashMap<>();

    Table<Long, Integer, Integer> FriendSettleAccount = HashBasedTable.create();
    Map<Long, Table<Long, Integer, Integer>> GroupSettleAccount = new HashMap<>();
    Map<Date, Long> GroupResetMark = new HashMap<>();
    Map<Date, Long> FriendResetMark = new HashMap<>();

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static final int GAP_SECONDS = 60;

    static final String RouletteRules = "里格斯公司邀请您参与本局 Roulette，请在60秒之内输入 /bet+数字 参与游戏。";
    static final String RouletteStops = "本局 Roulette 已经取消。";
    static final String NotRightBetNumber = "/bet 指令不正确，请重新再尝试";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";
    static final String StartBetNotice = "Bet 阶段已经开始，预计在60秒之内结束。可以通过/bet+金额反复追加 bet。\n在这一阶段不会向您收取南瓜比索。由于 Roulette 可以多重下注，因此不建议设置过大的 bet。";
    static final String EndBetNotice = "Bet 阶段已经结束。";
    static final String StartOperateNotice = "现在可以进行操作，请在60秒之内完成。功能列表请参考说明书。";
    static final String EndGameNotice = "本局游戏已经结束，里格斯公司感谢您的参与。如下为本局玩家获得的南瓜比索：";

    static final String ROULETTE_INTRO_PATH = "/pics/casino/roulette.png";
    static final String ROULETTE_INSTRUCTIONS_PATH = "/pics/casino/roulette_instructions.png";

    public static void go(MessageEvent event) {
        start(event);
        preBet(event);
        bet(event);
    }

    public static void start(MessageEvent event) {
        if (!isRoulette(event)) return;
        if (isInGamingProcess(event)) return;

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

        //清空一次数据
        cancelMark(event);

        InputStream img = Roulette.class.getResourceAsStream(ROULETTE_INTRO_PATH);
        assert img != null;
        GameCenterCount.count(GameCenterCount.Functions.RouletteStart);
        event.getSubject().sendMessage(new MessageChainBuilder().append(RouletteRules).append("\n\n").append(Contact.uploadImage(event.getSubject(), img)).asMessageChain());

        try {
            img.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (isGroupMessage(event)) {
            getINSTANCE().GroupStatusMap.put(event.getSubject().getId(), StatusType.Callin);
        } else {
            getINSTANCE().FriendStatusMap.put(event.getSubject().getId(), StatusType.Callin);
        }
        executor.schedule(new CancelCallin(event), GAP_SECONDS, TimeUnit.SECONDS);
        //3.5个间隔时间之后，取消标记
        executor.schedule(new CancelMarks(event, gameStartTime), (GAP_SECONDS * 3 + 2), TimeUnit.SECONDS);
    }

    //3个GAP_TIME之后取消所有标记的Runnable
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
                    //清除标记
                    Table<Long,Long,Integer> copyGroupBet = getINSTANCE().GroupBet;
                    for (Long playerID : copyGroupBet.row(event.getSubject().getId()).keySet()) {
                        getINSTANCE().GroupBet.remove(event.getSubject().getId(), playerID);
                    }
                    getINSTANCE().GroupSettleAccount.remove(event.getSubject().getId());
                    getINSTANCE().GroupStatusMap.remove(event.getSubject().getId());
                    getINSTANCE().GroupResetMark.remove(gameStartTime);
                }
            } else {
                if (getINSTANCE().FriendResetMark.containsKey(gameStartTime)) {
                    //清除标记
                    getINSTANCE().FriendBet.remove(event.getSubject().getId());
                    Table<Long,Integer,Integer> copyFriendSettleAccount = getINSTANCE().FriendSettleAccount;
                    for (Integer integer : copyFriendSettleAccount.row(event.getSubject().getId()).keySet()) {
                        getINSTANCE().FriendSettleAccount.remove(event.getSubject().getId(), integer);
                    }
                    getINSTANCE().FriendStatusMap.remove(event.getSubject().getId());
                    getINSTANCE().FriendResetMark.remove(gameStartTime);
                }
            }
        }
    }

    static void cancelMark(MessageEvent event) {
        if (isGroupMessage(event)) {
            //清除标记
            if(getINSTANCE().GroupBet.rowKeySet().contains(event.getSubject().getId())) {
                Table<Long,Long,Integer> copyGroupBet = getINSTANCE().GroupBet;
                for (Long playerID : copyGroupBet.row(event.getSubject().getId()).keySet()) {
                    getINSTANCE().GroupBet.remove(event.getSubject().getId(), playerID);
                }
            }
            getINSTANCE().GroupSettleAccount.remove(event.getSubject().getId());
            getINSTANCE().GroupStatusMap.remove(event.getSubject().getId());

            if(getINSTANCE().GroupResetMark.containsValue(event.getSubject().getId())){
                for(Date date:getINSTANCE().GroupResetMark.keySet()){
                    if (getINSTANCE().GroupResetMark.get(date).equals(event.getSubject().getId())){
                        getINSTANCE().GroupResetMark.remove(date);
                    }
                }
            }

        } else {
            //清除标记
            getINSTANCE().FriendBet.remove(event.getSubject().getId());
            Table<Long,Integer,Integer> copyFriendSettleAccount = getINSTANCE().FriendSettleAccount;
            if(getINSTANCE().FriendSettleAccount.rowKeySet().contains(event.getSubject().getId())){
                for (Integer integer : copyFriendSettleAccount.row(event.getSubject().getId()).keySet()) {
                    getINSTANCE().FriendSettleAccount.remove(event.getSubject().getId(), integer);
                }
            }
            getINSTANCE().FriendStatusMap.remove(event.getSubject().getId());
        }
    }

    //取消初始阶段的Runnable
    static class CancelCallin implements Runnable {
        MessageEvent event;

        CancelCallin(MessageEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            boolean isStillInCallin;
            if (isGroupMessage(event)) {
                isStillInCallin = getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.Callin);
            } else {
                isStillInCallin = getINSTANCE().FriendStatusMap.get(event.getSubject().getId()).equals(StatusType.Callin);
            }
            if (!isStillInCallin) return;
            event.getSubject().sendMessage(RouletteStops);
            if (isGroupMessage(event)) {
                getINSTANCE().GroupStatusMap.remove(event.getSubject().getId());
            } else {
                getINSTANCE().FriendStatusMap.remove(event.getSubject().getId());
            }
        }
    }

    //是不是群聊
    public static boolean isGroupMessage(MessageEvent event) {
        return (event.getClass().equals(GroupMessageEvent.class));
    }

    //给群聊的消息前面加AT
    public static MessageChainBuilder mcbProcessor(MessageEvent event) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (isGroupMessage(event)) {
            mcb.append((new At(event.getSender().getId()))).append(" ");
        }
        return mcb;
    }

    //判定是否有钱
    public static boolean hasEnoughMoney(MessageEvent event, int bet) {
        return PumpkinPesoWindow.hasEnoughMoney(event, bet);
    }

    //是否在游戏里
    public static boolean isInGamingProcess(MessageEvent event) {
        if (isGroupMessage(event)) {

            if (getINSTANCE().GroupStatusMap.containsKey(event.getSubject().getId())) {
                return !getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.End);
            } else {
                return false;
            }
        } else {
            return getINSTANCE().FriendStatusMap.containsKey(event.getSubject().getId());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void preBet(MessageEvent event) {
        if (!isBet(event)) return;
        if (!isInGamingProcess(event)) return;
        if (isGroupMessage(event)) {
            if (getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.Bet) || getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.End))
                return;
        } else {
            if (getINSTANCE().FriendStatusMap.get(event.getSubject().getId()).equals(StatusType.Bet) || getINSTANCE().FriendStatusMap.get(event.getSubject().getId()).equals(StatusType.End))
                return;
        }
        GameCenterCount.count(GameCenterCount.Functions.RouletteBet);

        Integer bet = null;

        try {
            bet = getBet(event.getMessage().contentToString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bet == null) {
            MessageChainBuilder mcb = mcbProcessor(event);
            event.getSubject().sendMessage(mcb.append(NotRightBetNumber).asMessageChain());
            return;
        }
        if (bet > 999999 || bet <= 0) {
            MessageChainBuilder mcb = mcbProcessor(event);
            event.getSubject().sendMessage(mcb.append(NotRightBetNumber).asMessageChain());
            return;
        }

        //没有足够多的钱
        if (!hasEnoughMoney(event, bet)) {
            MessageChainBuilder mcb = mcbProcessor(event);
            event.getSubject().sendMessage(mcb.append(YouDontHaveEnoughMoney).asMessageChain());
            return;
        }

        //累计
        if (isGroupMessage(event)) {
            if (getINSTANCE().GroupBet.row(event.getSubject().getId()).containsKey(event.getSender().getId())) {
                Integer beforeBet = getINSTANCE().GroupBet.get(event.getSubject().getId(), event.getSender().getId());
                System.out.println("累计进");
                //没有足够多的钱
                if (!hasEnoughMoney(event, beforeBet + bet)) {
                    MessageChainBuilder mcb = mcbProcessor(event);
                    event.getSubject().sendMessage(mcb.append(YouDontHaveEnoughMoney).asMessageChain());
                    return;
                } else {
                    System.out.println("有足够多的钱");
                    getINSTANCE().GroupBet.put(event.getSubject().getId(), event.getSender().getId(), beforeBet + bet);
                    MessageChainBuilder mcb = mcbProcessor(event);
                    event.getSubject().sendMessage(mcb.append("共收到下注").append(String.valueOf(beforeBet + bet)).append("南瓜比索").asMessageChain());
                    return;
                }
            }
        } else {
            if (getINSTANCE().FriendBet.containsKey(event.getSubject().getId())) {
                Integer beforeBet = getINSTANCE().FriendBet.get(event.getSubject().getId());
                System.out.println("累计进");
                //没有足够多的钱
                if (!hasEnoughMoney(event, bet + beforeBet)) {
                    event.getSubject().sendMessage(YouDontHaveEnoughMoney);
                } else {
                    System.out.println("有足够多的钱");
                    getINSTANCE().FriendBet.put(event.getSubject().getId(), beforeBet + bet);
                    MessageChainBuilder mcb = new MessageChainBuilder();
                    event.getSubject().sendMessage(mcb.append("共收到下注").append(String.valueOf(beforeBet + bet)).append("南瓜比索").asMessageChain());
                }
                return;
            }
        }

        //第一次进
        if (isGroupMessage(event)) {
            if (getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.Callin)) {
                getINSTANCE().GroupStatusMap.put(event.getSubject().getId(), StatusType.PreBet);
                executor.schedule(new EndPreBet(event), GAP_SECONDS, TimeUnit.SECONDS);
                event.getSubject().sendMessage(StartBetNotice);
            }
        } else {
            if (getINSTANCE().FriendStatusMap.get(event.getSubject().getId()).equals(StatusType.Callin)) {
                getINSTANCE().FriendStatusMap.put(event.getSubject().getId(), StatusType.PreBet);
                executor.schedule(new EndPreBet(event), 5, TimeUnit.SECONDS);
            }
        }

        //第一次往账户里面加钱
        if (isGroupMessage(event)) {
            if (!getINSTANCE().GroupBet.row(event.getSubject().getId()).containsKey(event.getSender().getId())) {
                System.out.println("第一次往账户里面加钱");
                getINSTANCE().GroupBet.put(event.getSubject().getId(), event.getSender().getId(), bet);
                MessageChainBuilder mcb = mcbProcessor(event);
                event.getSubject().sendMessage(mcb.append("已收到下注").append(String.valueOf(bet)).append("南瓜比索").asMessageChain());
            }
        } else {
            if (!getINSTANCE().FriendBet.containsKey(event.getSubject().getId())) {
                System.out.println("第一次往账户里面加钱");
                getINSTANCE().FriendBet.put(event.getSubject().getId(), bet);
                event.getSubject().sendMessage("已收到下注" + String.valueOf(bet) + "南瓜比索");
            }
        }
    }

    static class EndPreBet implements Runnable {

        private final MessageEvent event;

        EndPreBet(MessageEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            System.out.println("EndPreBetRunnable开始");
            if (isGroupMessage(event)) {
                if (getINSTANCE().GroupStatusMap.get(event.getSubject().getId()).equals(StatusType.Bet)) return;
            } else {
                if (getINSTANCE().FriendStatusMap.get(event.getSubject().getId()).equals(StatusType.Bet)) return;
            }
            EndPreBetActivities(event);
        }
    }

    static void EndPreBetActivities(MessageEvent event) {
        System.out.println("EndPreBetActivities开始");
        if (isGroupMessage(event)) {
            getINSTANCE().GroupStatusMap.put(event.getSubject().getId(), StatusType.Bet);
        } else {
            getINSTANCE().FriendStatusMap.put(event.getSubject().getId(), StatusType.Bet);
        }
        event.getSubject().sendMessage(new MessageChainBuilder().append(EndBetNotice).append(StartOperateNotice).asMessageChain());

        try (InputStream img = Roulette.class.getResourceAsStream(ROULETTE_INSTRUCTIONS_PATH)) {
            assert img != null;
            event.getSubject().sendMessage(Contact.uploadImage(event.getSubject(), img));
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.schedule(new EndBet(event), GAP_SECONDS, TimeUnit.SECONDS);
    }

    static class EndBet implements Runnable {
        private final MessageEvent event;

        EndBet(MessageEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            System.out.println("EndBetActivities开始");
            try {
                EndBetActivities(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void EndBetActivities(MessageEvent event) throws InterruptedException {
        try {
            if (isGroupMessage(event)) {
                getINSTANCE().GroupStatusMap.put(event.getSubject().getId(), StatusType.End);
            } else {
                getINSTANCE().FriendStatusMap.put(event.getSubject().getId(), StatusType.End);
            }
            System.out.println("进入EndBetActivties");
            event.getSubject().sendMessage("咚咚咚咚咚咚咚咚咚咚");
            Thread.sleep(1000 * 6);
            event.getSubject().sendMessage("咚—咚—咚—咚—咚—咚—");
            Thread.sleep(1000 * 6);
            event.getSubject().sendMessage("咚————咚————咚————");
            Thread.sleep(1000 * 6);
            Random random = new Random();
            int result = random.nextInt(37);
            MessageChainBuilder mcb = new MessageChainBuilder();
            mcb.append("球他妈停在了").append(String.valueOf(result)).append("上！");
            event.getSubject().sendMessage(mcb.asMessageChain());

            if (isGroupMessage(event)) {
                System.out.println("进入最终结算环节");
                MessageChainBuilder mcbg = new MessageChainBuilder();
                mcbg.append(EndGameNotice).append("\n");
                for (Long playerID : getINSTANCE().GroupSettleAccount.get(event.getSubject().getId()).rowKeySet()) {
                    try {
                        mcbg.append("\n").append(new At(playerID)).append(" ").append("共获得了").append(String.valueOf(getINSTANCE().GroupSettleAccount.get(event.getSubject().getId()).get(playerID, result) * getINSTANCE().GroupBet.get(event.getSubject().getId(), playerID))).append("南瓜比索");
                        PumpkinPesoWindow.addMoney(playerID, getINSTANCE().GroupSettleAccount.get(event.getSubject().getId()).get(playerID, result) * getINSTANCE().GroupBet.get(event.getSubject().getId(), playerID));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                event.getSubject().sendMessage(mcbg.asMessageChain());

            } else {
                MessageChainBuilder mcbf = new MessageChainBuilder();
                mcbf.append(EndGameNotice).append("\n");
                mcbf.append("\n").append("您获得了").append(String.valueOf(getINSTANCE().FriendSettleAccount.get(event.getSubject().getId(), result) * getINSTANCE().FriendBet.get(event.getSubject().getId()))).append("南瓜比索。");

                PumpkinPesoWindow.addMoney(event.getSubject().getId(), getINSTANCE().FriendSettleAccount.get(event.getSubject().getId(), result) * getINSTANCE().FriendBet.get(event.getSubject().getId()));
                event.getSubject().sendMessage(mcbf.asMessageChain());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (isGroupMessage(event)) {
                //清除标记
                for (Date date : getINSTANCE().GroupResetMark.keySet()) {
                    if (getINSTANCE().GroupResetMark.get(date) == event.getSubject().getId()) {
                        getINSTANCE().GroupResetMark.remove(date);
                        break;
                    }
                }
                try {
                    Table<Long,Long,Integer> copyGroupBet = getINSTANCE().GroupBet;
                    for (Long playerID : copyGroupBet.row(event.getSubject().getId()).keySet()) {
                        getINSTANCE().GroupBet.remove(event.getSubject().getId(), playerID);
                    }
                    getINSTANCE().GroupSettleAccount.remove(event.getSubject().getId());
                    getINSTANCE().GroupStatusMap.remove(event.getSubject().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                //清除标记
                getINSTANCE().FriendBet.remove(event.getSubject().getId());
                Table<Long,Integer,Integer> copyFriendSettleAccount = getINSTANCE().FriendSettleAccount;
                for (Integer integer : copyFriendSettleAccount.row(event.getSubject().getId()).keySet()) {
                    getINSTANCE().FriendSettleAccount.remove(event.getSubject().getId(), integer);
                }
                try {
                    for (Date date : getINSTANCE().FriendResetMark.keySet()) {
                        if (getINSTANCE().FriendResetMark.get(date) == event.getSubject().getId()) {
                            getINSTANCE().FriendResetMark.remove(date);
                            break;
                        }
                    }
                    getINSTANCE().FriendStatusMap.remove(event.getSubject().getId());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void bet(MessageEvent event) {
        if (isGroupMessage(event)) {
            if (getINSTANCE().GroupStatusMap.get(event.getSubject().getId()) != StatusType.Bet) return;
        } else {
            if (getINSTANCE().FriendStatusMap.get(event.getSubject().getId()) != StatusType.Bet) return;
        }

        if (!hasIndicator(event.getMessage().contentToString())) return;

        if (isGroupMessage(event)) {
            if (!getINSTANCE().GroupBet.rowKeySet().contains(event.getSubject().getId())) return;
            if (!getINSTANCE().GroupBet.row(event.getSubject().getId()).containsKey(event.getSender().getId())) return;
        } else {
            if (!getINSTANCE().FriendBet.containsKey(event.getSubject().getId())) return;
        }

        GameCenterCount.count(GameCenterCount.Functions.RouletteOperations);

        List<RouletteBet> rouletteBetList = processString(event.getMessage().contentToString());
        List<RouletteBet> trueBetList = getDeFactoBets(rouletteBetList);
        int betAmount = getBetAmount(trueBetList);

        //要是没钱
        if (isGroupMessage(event)) {
            int playersBet = getINSTANCE().GroupBet.get(event.getSubject().getId(), event.getSender().getId()) * betAmount;
            if (!hasEnoughMoney(event, playersBet)) {
                event.getSubject().sendMessage(YouDontHaveEnoughMoney);
                return;
            }
        } else {
            int playersBet = getINSTANCE().FriendBet.get(event.getSubject().getId()) * betAmount;
            if (!hasEnoughMoney(event, playersBet)) {
                event.getSubject().sendMessage(YouDontHaveEnoughMoney);
                return;
            }
        }

        //有钱就走账了哈
        if (isGroupMessage(event)) {
            System.out.println("计算playersBet");
            int playersBet = getINSTANCE().GroupBet.get(event.getSubject().getId(), event.getSender().getId()) * betAmount;
            System.out.println("玩家赌注：" + playersBet);
            //如果是第一次下注则初始化
            if (hasNeverGivenAnyIndicatorInGroup(event)) {
                setNewMapForGroup(event);
            }
            updateMap(trueBetList, event.getSender().getId(), event.getSubject().getId());
            PumpkinPesoWindow.minusMoney(event.getSender().getId(), playersBet);
        } else {
            System.out.println("计算playersBet");
            int playersBet = getINSTANCE().FriendBet.get(event.getSubject().getId()) * betAmount;
            System.out.println("玩家赌注：" + playersBet);
            //如果是第一次下注则初始化
            if (hasNeverGivenAnyIndicatorInFriend(event)) {
                setNewTableForFriend(event);
            }
            updateTable(trueBetList, event.getSubject().getId());
            PumpkinPesoWindow.minusMoney(event.getSender().getId(), playersBet);
        }

        MessageChainBuilder mcb = mcbProcessor(event);
        event.getSubject().sendMessage(mcb.append(feedbeckBetStatus(rouletteBetList)).append("\n").append("共收到").append(String.valueOf(betAmount)).append("注。").asMessageChain());

    }

    static boolean hasNeverGivenAnyIndicatorInGroup(MessageEvent event) {
        return !getINSTANCE().GroupSettleAccount.containsKey(event.getSubject().getId()) ||
                !getINSTANCE().GroupSettleAccount.get(event.getSubject().getId()).rowKeySet().contains(event.getSender().getId());
    }

    static boolean hasNeverGivenAnyIndicatorInFriend(MessageEvent event) {
        return !getINSTANCE().FriendSettleAccount.rowKeySet().contains(event.getSubject().getId());
    }

}
