package mirai.chitung.plugin.utils;


import mirai.chitung.plugin.administration.config.ConfigHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GroupPolice {

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    final static GroupPolice INSTANCE = new GroupPolice();

    GroupPolice() {
        executor.scheduleAtFixedRate(new AutoClear(), (long)0.5, 3, TimeUnit.HOURS);
    }

    static public GroupPolice getINSTANCE() {
        return INSTANCE;
    }

    static class AutoClear implements Runnable {
        @Override
        public void run() {
            for (Bot bot : Bot.getInstances()) {
                for (Group group : bot.getGroups()) {
                    if (IdentityUtil.isDevGroup(group.getId())) continue;
                    if (group.getMembers().getSize() < ConfigHandler.getINSTANCE().config.getMinimumMembers()) {
                        group.sendMessage(ConfigHandler.getName(bot)+"目前不接受加入"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人以下的群聊，将自动退群，请在其他群中使用"+ConfigHandler.getName(bot)+"。感谢您使用"+ConfigHandler.getName(bot)+"的服务。");
                        MessageUtil.notifyDevGroup("由于群聊人数不满"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人，"+ConfigHandler.getName(bot)+"已经从"+group.getName()+"("+group.getId()+")中离开。",bot);
                        executor.schedule(() -> {
                            Objects.requireNonNull(bot.getGroup(group.getId())).quit();
                        }, 15, TimeUnit.SECONDS);
                    }
                }
            }
        }
    }

    public void ini(){
        System.out.println("Initialize Group Police");
    }
}
