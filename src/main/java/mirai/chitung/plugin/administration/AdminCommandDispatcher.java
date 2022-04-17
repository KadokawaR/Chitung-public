package mirai.chitung.plugin.administration;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.responder.Blacklist;
import mirai.chitung.plugin.utils.IdentityUtil;
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
        //黑名单
        Blacklist.operation(event);
        //设置管理
        ConfigHandler.react(event);
        //管理帮助
        AdminHelp.send(event);
    }

    public void close() {
        executor.shutdown();
    }
}
