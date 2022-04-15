package mirai.chitung.plugin.utils;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.responder.help.NewHelp;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContactUtil {
    public static String JOIN_GROUP = ConfigHandler.getINSTANCE().config.getCc().getJoinGroupText();
    public static String DISCLAIMER = NewHelp.Speech.DISCL;

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 决定是否接收加群邀请
    public static void handleGroupInvitation(BotInvitedJoinGroupRequestEvent event) {
        if(IdentityUtil.isAdmin(Objects.requireNonNull(event.getInvitor()).getId())) {
            event.accept();
            return;
        }
        if (!ConfigHandler.canAddGroup()) {
            if(ConfigHandler.canAutoAnswer()) {
                event.getInvitor().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getRejectGroupText());
            }
            event.ignore();
        } else {
            event.accept();
        }

    }

    // 决定是否接收好友请求
    public static void handleFriendRequest(NewFriendRequestEvent event) {
        if(IdentityUtil.isAdmin(event.getFromId())) event.accept();
        if (!ConfigHandler.canAddFriend()) {
            event.reject(false);
            return;
        }
        event.accept();
    }

    // 处理加群事件
    public static void handleJoinGroup(BotJoinGroupEvent.Invite event) {
        if(IdentityUtil.isDevGroup(event.getGroupId())){
            event.getGroup().sendMessage("反正是开发组，咱没话说。");
            return;
        }
        executor.schedule(() -> {
            //管理员判定
            if(!IdentityUtil.isAdmin((event).getInvitor().getId())) {
                if (!ConfigHandler.canAddGroup()) {
                    event.getGroup().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getRejectGroupText());
                    if(ConfigHandler.canAutoAnswer()) {
                        (event).getInvitor().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getRejectGroupText());
                    }
                    event.getGroup().quit();
                    String content = "由于目前"+ConfigHandler.getName(event)+"不接受添加群聊，已经从 " + event.getGroup().getName() + "(" + event.getGroup().getId() + ")" + "出逃。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }

                if (event.getGroup().getMembers().getSize() < ConfigHandler.getINSTANCE().config.getMinimumMembers()) {
                    event.getGroup().sendMessage(ConfigHandler.getName(event)+"目前不接受加入"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人以下的群聊，将会自动退群。");
                    if(ConfigHandler.canAutoAnswer()) {
                        (event).getInvitor().sendMessage(ConfigHandler.getName(event)+"目前不接受加入"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人以下的群聊，将会自动退群。");
                    }
                    event.getGroup().quit();
                    String content = (event).getInvitor().getNick() + "(" + (event).getInvitor().getId() + ")尝试邀请"+ConfigHandler.getName(event)+"加入一个少于"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人的群聊。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }
            }

            // 正常通过群邀请加群
            sendNoticeWhenJoinGroup(event.getGroup(),event.getBot());
            notifyDevWhenJoinGroup(event);

            event.getGroup().sendMessage(JOIN_GROUP);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.getGroup().sendMessage(DISCLAIMER);
        },10,TimeUnit.SECONDS);

    }

    // 处理加群事件
    public static void handleJoinGroup(BotJoinGroupEvent.Active event) {
        if(IdentityUtil.isDevGroup(event.getGroupId())){
            return;
        }
        executor.schedule(() -> {

                if (!ConfigHandler.canAddGroup()) {
                    event.getGroup().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getRejectGroupText());
                    event.getGroup().quit();
                    String content = "由于目前"+ConfigHandler.getName(event)+"不接受添加群聊，已经从 " + event.getGroup().getName() + "(" + event.getGroup().getId() + ")" + "离开。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }

                if (event.getGroup().getMembers().getSize() < ConfigHandler.getINSTANCE().config.getMinimumMembers()) {
                    event.getGroup().sendMessage(ConfigHandler.getName(event)+"目前不接受加入"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人以下的群聊。");
                    event.getGroup().quit();
                    String content = "有人尝试尝试邀请"+ConfigHandler.getName(event)+"加入一个少于"+ConfigHandler.getINSTANCE().config.getMinimumMembers()+"人的群聊 "+event.getGroup().getName()+"("+event.getGroup().getId()+")，已经离开";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }

            // 正常通过群邀请加群
            sendNoticeWhenJoinGroup(event.getGroup(),event.getBot());
            notifyDevWhenJoinGroup(event);

            event.getGroup().sendMessage(JOIN_GROUP);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.getGroup().sendMessage(DISCLAIMER);
        },10,TimeUnit.SECONDS);

    }

    // 处理退群事件
    public static void handleLeaveGroup(BotLeaveEvent.Kick event) {
        // 通知开发者群
        notifyDevWhenLeaveGroup(event);
    }

    // 处理退群事件
    public static void handleLeaveGroup(BotLeaveEvent.Active event) {
        // 通知开发者群
        notifyDevWhenLeaveGroup(event);
    }

    // 处理加为好友事件
    public static void handleAddFriend(FriendAddEvent event) {
        if(!ConfigHandler.canAutoAnswer()) return;
        executor.schedule(() -> {
            event.getFriend().sendMessage(JOIN_GROUP);
            event.getFriend().sendMessage(DISCLAIMER);
        },15, TimeUnit.SECONDS);
    }

    //尝试退出某个群
    public static void tryQuitGroup(long id) {
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Group group = bot.getGroup(id);
            if (group != null) group.quit();
        }
    }

    //尝试删除好友
    public static void tryDeleteFriend(long id) {
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Friend friend = bot.getFriend(id);
            if (friend != null) friend.delete();
        }
    }

    // 加群后发送Bot提示
    static void sendNoticeWhenJoinGroup(BotJoinGroupEvent.Active event) {
        if(!ConfigHandler.canAutoAnswer()) return;
        String message = "您好，"+ConfigHandler.getName(event)+"已经加入了您的群" + event.getGroup().getName() + " - " + event.getGroup().getId() + "，请在群聊中输入/help 以获取相关信息。如果"+ConfigHandler.getName(event)+"过于干扰群内秩序，请将它从您的群中移除。";
        event.getGroup().getOwner().sendMessage(message);
    }

    static void sendNoticeWhenJoinGroup(Group group, Bot bot) {
        if(!ConfigHandler.canAutoAnswer()) return;
        String message = "您好，"+ConfigHandler.getName(bot)+"已经加入了您的群" + group.getName() + " - " + group.getId() + "，请在群聊中输入/help 以获取相关信息。如果"+ConfigHandler.getName(bot)+"过于干扰群内秩序，请将它从您的群中移除。";
        group.getOwner().sendMessage(message);
    }

    // 向开发者发送加群提醒
    static void notifyDevWhenJoinGroup(BotJoinGroupEvent.Invite event) {
        MessageUtil.notifyDevGroup(ConfigHandler.getName(event)+"已加入 " + event.getGroup().getName() + "（" + event.getGroupId() + "）,邀请人为 "
                + ((BotJoinGroupEvent.Invite) event).getInvitor().getNick() + "（" + ((BotJoinGroupEvent.Invite) event).getInvitor().getId() + "）。", event.getBot().getId());
    }

    // 向开发者发送加群提醒
    static void notifyDevWhenJoinGroup(BotJoinGroupEvent.Active event) {
        MessageUtil.notifyDevGroup(ConfigHandler.getName(event)+"已加入 " + event.getGroup().getName() + "（" + event.getGroupId() + "）", event.getBot().getId());
    }

    // 向开发者发送退群提醒
    static void notifyDevWhenLeaveGroup(BotLeaveEvent.Kick event) {
        //todo:Mirai开发者告知这个写法可能不稳定
        MessageUtil.notifyDevGroup(ConfigHandler.getName(event)+"已经从 " + event.getGroup().getName() + "（" + event.getGroupId() + "）离开，由管理员操作。", event.getBot().getId());
    }

    // 向开发者发送退群提醒
    static void notifyDevWhenLeaveGroup(BotLeaveEvent.Active event) {
        //todo:Mirai开发者告知这个写法可能不稳定
        MessageUtil.notifyDevGroup(ConfigHandler.getName(event)+"已经从 " + event.getGroup().getName() + "（" + event.getGroupId() + "）主动离开。", event.getBot().getId());
    }

}
