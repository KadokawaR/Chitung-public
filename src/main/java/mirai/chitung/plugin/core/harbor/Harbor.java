package mirai.chitung.plugin.core.harbor;

import com.google.common.collect.Maps;
import mirai.chitung.plugin.utils.IdentityUtil;
import mirai.chitung.plugin.utils.StandardTimeUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Harbor {
    final static Map<String, Threshold> MIN_THRESHOLDS = new ConcurrentHashMap<>();
    final static Map<String, Threshold> DAY_THRESHOLDS = new ConcurrentHashMap<>();
    final static Timer thresholdReset1 = new Timer(true);
    final static Timer thresholdReset2 = new Timer(true);

    static {
        thresholdReset1.schedule(new TimerTask() {
                                     @Override
                                     public void run() {
                                         for (Threshold threshold : MIN_THRESHOLDS.values()) {
                                             threshold.clear();
                                         }
                                     }
                                 }, StandardTimeUtil.getPeriodLengthInMS(0, 0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(0, 0, 1, 0));
        thresholdReset2.schedule(new TimerTask() {
                                     @Override
                                     public void run() {
                                         MIN_THRESHOLDS.clear();
                                         DAY_THRESHOLDS.clear();
                                     }
                                 }, StandardTimeUtil.getStandardFirstTime(0, 0, 1),
                StandardTimeUtil.getPeriodLengthInMS(1, 0, 0, 0));
    }

    static Threshold[] acquire(PortRequestInfo requestInfo) {
        if (MIN_THRESHOLDS.get(requestInfo.tag) == null) {
            MIN_THRESHOLDS.put(requestInfo.tag, new Threshold(requestInfo.minute_limit));
        }
        if (DAY_THRESHOLDS.get(requestInfo.tag) == null) {
            DAY_THRESHOLDS.put(requestInfo.tag, new Threshold(requestInfo.daily_limit));
        }
        return new Threshold[]{MIN_THRESHOLDS.get(requestInfo.tag), DAY_THRESHOLDS.get(requestInfo.tag)};
    }

    public static boolean isReachingPortLimit(PortRequestInfo requestInfo, long id) {
        Threshold[] thresholds = acquire(requestInfo);
        return thresholds[0].reachLimit(id) || thresholds[1].reachLimit(id);
    }

    public static void count(PortRequestInfo requestInfo, long id) {
        Threshold[] thresholds = acquire(requestInfo);
        thresholds[0].count(id);
        thresholds[1].count(id);
    }

    public static void count(MessageEvent event) {
        if(event instanceof GroupMessageEvent){
            count(PortRequestInfos.GROUP_MINUTE,((GroupMessageEvent) event).getGroup().getId());
        }
        count(PortRequestInfos.PERSONAL,event.getSender().getId());
        count(PortRequestInfos.TOTAL_DAILY,0);
    }

    public static int getMinutePortRecordById(PortRequestInfo requestInfo, long id) {
        return acquire(requestInfo)[0].get(id);
    }

    public static int getDailyPortRecordById(PortRequestInfo requestInfo, long id) {
        return acquire(requestInfo)[1].get(id);
    }

    public static Map<Long, Integer> getMinutePortRecord(PortRequestInfo requestInfo) {
        return Maps.newHashMap(acquire(requestInfo)[0].data);
    }

    public static Map<Long, Integer> getDailyPortRecord(PortRequestInfo requestInfo) {
        return Maps.newHashMap(acquire(requestInfo)[1].data);
    }


    public static void clearPortRecordManually(PortRequestInfo requestInfo) {
        Threshold[] thresholds = acquire(requestInfo);
        thresholds[0].clear();
        thresholds[1].clear();
    }

    public static boolean isReachingPortLimit(MessageEvent event){
        if(IdentityUtil.isAdmin(event.getSender().getId())) return false;
        if(isReachingPortLimit(PortRequestInfos.TOTAL_DAILY, 0)) return true;
        if(event instanceof GroupMessageEvent){
            return isReachingPortLimit(PortRequestInfos.PERSONAL,event.getSender().getId())||isReachingPortLimit(PortRequestInfos.GROUP_MINUTE,((GroupMessageEvent) event).getGroup().getId());
        } else {
            return isReachingPortLimit(PortRequestInfos.PERSONAL,event.getSender().getId());
        }
    }


}
