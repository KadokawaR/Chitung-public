package lielietea.mirai.plugin.administration.statistics.MPSEHandler;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lielietea.mirai.plugin.utils.IdentityUtil;
import lielietea.mirai.plugin.utils.MessageUtil;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.MessagePostSendEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessagePostSendEventHandler extends MPSEStatistics {


    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    Table<MultiBotHandler.BotName,MessageKind,Integer> messageCountTable;

    DataList dataList = new DataList();
    Map<Long,Boolean> triggerBreakMap = new HashMap<>();
    static MainTask mainTask = new MainTask();

    MessagePostSendEventHandler() {
    }

    final static MessagePostSendEventHandler INSTANCE;

    static {
        INSTANCE = new MessagePostSendEventHandler();
        getINSTANCE().messageCountTable = HashBasedTable.create();
        for(MultiBotHandler.BotName bn: MultiBotHandler.BotName.values()){
            getINSTANCE().messageCountTable.put(Objects.requireNonNull(MultiBotHandler.BotName.get(bn.getValue())), MessageKind.FriendMessage, 0);
            getINSTANCE().messageCountTable.put(Objects.requireNonNull(MultiBotHandler.BotName.get(bn.getValue())), MessageKind.GroupMessage, 0);
            getINSTANCE().messageCountTable.put(Objects.requireNonNull(MultiBotHandler.BotName.get(bn.getValue())), MessageKind.FailedMessage, 0);
            getINSTANCE().triggerBreakMap.put(bn.getValue(),false);
        }
        executor.schedule(mainTask, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(mainTask, 5, 5, TimeUnit.MINUTES);
    }

    static public MessagePostSendEventHandler getINSTANCE() {
        return INSTANCE;
    }

    public static void updateCount(MultiBotHandler.BotName bn, MessageKind mk, int num){
        getINSTANCE().messageCountTable.put(bn,mk,getINSTANCE().messageCountTable.get(bn,mk)+num);
    }

    public static void count(MessagePostSendEvent event) {
        if (event.getReceipt() == null) {
            updateCount(MultiBotHandler.BotName.get(event.getBot().getId()),MessageKind.FailedMessage,1);
            System.out.println("failedMessageCount++");
            MessageChainBuilder mcb = new MessageChainBuilder();
            mcb.append(Objects.requireNonNull(event.getException()).getMessage()).append("\n");
            mcb.append(String.valueOf(event.getTarget().getId()));
            MessageUtil.notifyDevGroup(mcb.asMessageChain(),event.getBot().getId());
            return;
        }

        if (event.getReceipt().isToGroup()) {
            updateCount(MultiBotHandler.BotName.get(event.getBot().getId()),MessageKind.GroupMessage,1);
            System.out.println("groupMessageCount++");
        } else {
            updateCount(MultiBotHandler.BotName.get(event.getBot().getId()),MessageKind.FriendMessage,1);
            System.out.println("friendMessageCount++");
        }
    }

    public static void updateDataList(long botID) {
        int groupMessageCount = getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID),MessageKind.GroupMessage);
        int friendMessageCount = getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID),MessageKind.FriendMessage);
        int failedMessageCount = getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID),MessageKind.FailedMessage);

        Date date = new Date();
        Date dateAWeekAgo = updateDaysByGetTime(date, -7);
        Data data = new Data(date, friendMessageCount, groupMessageCount, failedMessageCount, MultiBotHandler.BotName.get(botID));
        getINSTANCE().dataList.addDataIntoDatas(data);
        getINSTANCE().dataList.datas.removeIf(dt -> dt.getDate().before(dateAWeekAgo));

    }

    public static void resetCount() {
        for(MultiBotHandler.BotName bn: MultiBotHandler.BotName.values()){
            getINSTANCE().messageCountTable.put(bn,MessageKind.FriendMessage,0);
            getINSTANCE().messageCountTable.put(bn,MessageKind.GroupMessage,0);
            getINSTANCE().messageCountTable.put(bn,MessageKind.FailedMessage,0);
        }
    }

    static class MainTask implements Runnable {
        @Override
        public void run() {
            try {
                getINSTANCE().dataList = openData();
                for(Bot bot : Bot.getInstances()){
                    boolean originalStatus = getINSTANCE().triggerBreakMap.get(bot.getId());
                    updateDataList(bot.getId());
                    boolean newStatus = triggeredBreaker(bot.getId());
                    getINSTANCE().triggerBreakMap.put(bot.getId(),newStatus);
                    if(originalStatus!=newStatus){
                        MessageUtil.notifyDevGroup("熔断机制状态发生变化，目前的熔断状况是 "+String.valueOf(newStatus),bot.getId());
                    }
                }
                writeData(getINSTANCE().dataList);
                resetCount();
                System.out.println("MPSE已更新。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handle(MessagePostSendEvent event) {
        count(event);
    }

    public enum MessageKind{
        FriendMessage,
        GroupMessage,
        FailedMessage
    }

    public static boolean botHasTriggeredBreak(GroupMessageEvent event){
        if(IdentityUtil.isAdmin(event)) return false;
        return getINSTANCE().triggerBreakMap.get(event.getBot().getId());
    }

    public static void checkBreaker(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().contains("/break")){
            event.getSubject().sendMessage("目前的熔断情况是: "+getINSTANCE().triggerBreakMap.get(event.getBot().getId()));
        }
    }

}
