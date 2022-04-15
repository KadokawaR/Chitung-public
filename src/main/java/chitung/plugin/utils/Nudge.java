package chitung.plugin.utils;

import chitung.plugin.administration.config.ConfigHandler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Nudge {

    static final int MAX_COUNT = 4;
    static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10);

    Nudge(){}

    private static final Nudge INSTANCE;

    static class clearCountTask implements Runnable{
        @Override
        public void run() {
            getINSTANCE().groupNudgeCount.clear();
        }
    }

    static {
        INSTANCE = new Nudge();
        EXECUTOR.scheduleAtFixedRate(new clearCountTask(),1,1, TimeUnit.MINUTES);
    }

    private final Map<Long,Integer> groupNudgeCount = new HashMap<>();

    public static Nudge getINSTANCE() {
        return INSTANCE;
    }

    private static void addCount(long groupID){
        if(!getINSTANCE().groupNudgeCount.containsKey(groupID)){
            getINSTANCE().groupNudgeCount.put(groupID,1);
            return;
        }
        int count = getINSTANCE().groupNudgeCount.get(groupID);
        getINSTANCE().groupNudgeCount.remove(groupID);
        getINSTANCE().groupNudgeCount.put(groupID,count+1);
    }

    private static boolean overCount(long groupID){
        if(!getINSTANCE().groupNudgeCount.containsKey(groupID)) return true;
        return getINSTANCE().groupNudgeCount.get(groupID)>MAX_COUNT;
    }

    public static void returnNudge(NudgeEvent event){
        if(!(event.getSubject() instanceof Group)) return;
        if(overCount(event.getSubject().getId())) return;
        if(IdentityUtil.isBot(event.getFrom().getId())) return;
        if (event.getTarget().equals(event.getBot())){
            event.getFrom().nudge().sendTo(event.getSubject());
            event.getSubject().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getNudgeText());
            addCount(event.getSubject().getId());
        }
    }

    public static void mentionNudge(GroupMessageEvent event){
        if(IdentityUtil.isBot(event.getSender().getId())) return;
        if(overCount(event.getGroup().getId())) return;
        if (event.getMessage().contentToString().contains(String.valueOf(event.getBot().getId()))){
            event.getSender().nudge().sendTo(event.getSubject());
            addCount(event.getSubject().getId());
        }
    }
}
