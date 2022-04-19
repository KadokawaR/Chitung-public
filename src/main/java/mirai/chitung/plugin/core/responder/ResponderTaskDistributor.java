package mirai.chitung.plugin.core.responder;
import mirai.chitung.plugin.core.harbor.Harbor;
import mirai.chitung.plugin.core.harbor.PortRequestInfos;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ResponderTaskDistributor {
    final static ExecutorService ISOLATED_EXECUTOR = Executors.newSingleThreadExecutor();
    final ExecutorService executor;

    public ResponderTaskDistributor() {
        this.executor = Executors.newCachedThreadPool();
    }

    public void handleMessage(MessageEvent event) {
        // 需要没有达到消息数限制
        if (!reachLimit(event)) {
            Optional<UUID> boxedHandler = ResponderManager.getINSTANCE().match(event);
            if (boxedHandler.isPresent()) {
                {
                    RespondTask task = ResponderManager.getINSTANCE().handle(event, boxedHandler.get());
                    addToThreshold(task);
                    handleResponderTask(task);
                }
            }
        }
    }

    public static void handleIsolatedResponderTask(RespondTask respondTask){
        if (respondTask.getSource() instanceof Group)
            Harbor.count(PortRequestInfos.GROUP_MINUTE,respondTask.getSource().getId());
        Harbor.count(PortRequestInfos.PERSONAL,respondTask.getSender().getId());
        Harbor.count(PortRequestInfos.TOTAL_DAILY,0L);
        ISOLATED_EXECUTOR.submit(respondTask::execute);
    }

    // 检测是否达到发送消息数量上限
    boolean reachLimit(MessageEvent event) {
        if (Harbor.isReachingPortLimit(PortRequestInfos.TOTAL_DAILY,0L)) return true;
        if (event instanceof GroupMessageEvent) {
            if (Harbor.isReachingPortLimit(PortRequestInfos.GROUP_MINUTE,event.getSubject().getId()))
                return true;
        }
        return Harbor.isReachingPortLimit(PortRequestInfos.PERSONAL,event.getSubject().getId());
    }

    void addToThreshold(RespondTask respondTask) {
        if (respondTask.getSource() instanceof Group)
            Harbor.count(PortRequestInfos.GROUP_MINUTE,respondTask.getSource().getId());
        Harbor.count(PortRequestInfos.PERSONAL,respondTask.getSender().getId());
        Harbor.count(PortRequestInfos.TOTAL_DAILY,0L);
    }

    void handleResponderTask(RespondTask respondTask) {
        executor.submit(respondTask::execute);
    }

    public void close() {
        executor.shutdown();
        if(!ISOLATED_EXECUTOR.isShutdown()){
            ISOLATED_EXECUTOR.shutdown();
        }
    }

}
