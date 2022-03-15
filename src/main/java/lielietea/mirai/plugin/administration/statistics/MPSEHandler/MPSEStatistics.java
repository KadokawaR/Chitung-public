package lielietea.mirai.plugin.administration.statistics.MPSEHandler;

import lielietea.mirai.plugin.core.responder.ResponderManager;
import lielietea.mirai.plugin.utils.IdentityUtil;
import lielietea.mirai.plugin.utils.MessageUtil;
import lielietea.mirai.plugin.utils.StandardTimeUtil;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MPSEStatistics extends MPSEProcessor{

    final static int DAILY_THRESHOLD = 4500;
    final static int HALF_DAY_THRESHOLD = 2500;
    final static int SIX_HOUR_THRESHOLD = 1500;
    final static int THREE_HOUR_THRESHOLD = 1000;
    final static int ONE_HOUR_THRESHOLD = 500;
    static final Timer TIMER = new Timer(true);

    static{
        TIMER.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               for(Bot bot: Bot.getInstances()){
                                   MessageUtil.notifyDevGroup(MPSEStatistics.buildMPSEStatistics(bot.getId()), bot);
                               }

                           }
                       },
                StandardTimeUtil.getStandardFirstTime(0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(0, 6, 0, 0));
    }

    public static Data getXHourMPSEData(int hour,long botID){
        DataList dl = MessagePostSendEventHandler.getINSTANCE().dataList;
        Date now = new Date();
        Date hourAgo = updateMinutesByGetTime(now,-(hour*60+5));
        int friendMessageCount=0;
        int groupMessageCount=0;
        int failedMessageCount=0;
        for(Data dt : dl.datas){
            if(dt.getBn().getValue()!=botID) continue;
            if(dt.getDate().after(hourAgo)){
                friendMessageCount += dt.getFriendMessage();
                groupMessageCount += dt.getGroupMessage();
                failedMessageCount += dt.getFailedMessage();
            }
        }
        friendMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.FriendMessage);
        groupMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.GroupMessage);
        failedMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.FailedMessage);
        return new Data(friendMessageCount,groupMessageCount,failedMessageCount, MultiBotHandler.BotName.get(botID));
    }

    public static Data getAllMPSEData(long botID){
        DataList dl = MessagePostSendEventHandler.getINSTANCE().dataList;
        int friendMessageCount=0;
        int groupMessageCount=0;
        int failedMessageCount=0;
        for(Data dt : dl.datas){
            if (dt.getBn().getValue()!=botID) continue;
            friendMessageCount += dt.getFriendMessage();
            groupMessageCount += dt.getGroupMessage();
            failedMessageCount += dt.getFailedMessage();
        }
        friendMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.FriendMessage);
        groupMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.GroupMessage);
        failedMessageCount += MessagePostSendEventHandler.getINSTANCE().messageCountTable.get(MultiBotHandler.BotName.get(botID), MessagePostSendEventHandler.MessageKind.FailedMessage);
        return new Data(friendMessageCount,groupMessageCount,failedMessageCount, MultiBotHandler.BotName.get(botID));
    }

    public static String fromDataToString(Data data){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(data.getFriendMessage()).append("\t");
        stringBuilder.append(data.getGroupMessage()).append("\t");
        stringBuilder.append(data.getFailedMessage()).append("\t");
        return stringBuilder.toString();
    }

    public static void getMPSEStatistics(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().equals("/total")||event.getMessage().contentToString().equals("统计数据")){
            event.getSubject().sendMessage(buildMPSEStatistics(event.getBot().getId()));
        }
    }

    public static String buildMPSEStatistics(long botID){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MPSE  \t").append("好友 \t群组 \t失败\n");
        stringBuilder.append("7 day \t").append(fromDataToString(getAllMPSEData(botID))).append("\n");
        stringBuilder.append("3 day \t").append(fromDataToString(getXHourMPSEData(24*3,botID))).append("\n");
        stringBuilder.append("1 day \t").append(fromDataToString(getXHourMPSEData(24,botID))).append("\n");
        stringBuilder.append("½ day \t").append(fromDataToString(getXHourMPSEData(12,botID))).append("\n");
        stringBuilder.append("6 hour\t").append(fromDataToString(getXHourMPSEData(6,botID))).append("\n");
        stringBuilder.append("3 hour\t").append(fromDataToString(getXHourMPSEData(3,botID))).append("\n");
        stringBuilder.append("1 hour\t").append(fromDataToString(getXHourMPSEData(1,botID)));
        return stringBuilder.toString();
    }

    public static boolean triggeredBreaker(long BotID){
        return getXHourMPSEData(24,BotID).getGroupMessage()>=DAILY_THRESHOLD||
                getXHourMPSEData(12,BotID).getGroupMessage()>=HALF_DAY_THRESHOLD||
                getXHourMPSEData(6,BotID).getGroupMessage()>=SIX_HOUR_THRESHOLD||
                getXHourMPSEData(3,BotID).getGroupMessage()>=THREE_HOUR_THRESHOLD||
                getXHourMPSEData(1,BotID).getGroupMessage()>=ONE_HOUR_THRESHOLD;
    }
}
