package lielietea.mirai.plugin.administration;

import lielietea.mirai.plugin.administration.config.ConfigHandler;
import lielietea.mirai.plugin.administration.statistics.GameCenterCount;
import lielietea.mirai.plugin.administration.statistics.MPSEHandler.MessagePostSendEventHandler;
import lielietea.mirai.plugin.utils.IdentityUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminCommandDispatcher {
    static final AdminCommandDispatcher INSTANCE = new AdminCommandDispatcher();
    final ExecutorService executor;


    public AdminCommandDispatcher() {
        this.executor = Executors.newCachedThreadPool();
    }

    public static AdminCommandDispatcher getInstance() {
        return INSTANCE;
    }


    public void handleMessage(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        AdminTools.getINSTANCE().handleAdminCommand(event);
        //MPSE 消息统计
        MessagePostSendEventHandler.getMPSEStatistics(event);
        MessagePostSendEventHandler.checkBreaker(event);
        //设置管理
        ConfigHandler.react(event);
        //GameCenter统计
        GameCenterCount.getStatistics(event);
    }

    public void close() {
        executor.shutdown();
    }
}
