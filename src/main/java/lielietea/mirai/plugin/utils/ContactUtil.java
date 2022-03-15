package lielietea.mirai.plugin.utils;

import lielietea.mirai.plugin.core.responder.help.DisclTemporary;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContactUtil {
    public static final String JOIN_GROUP = "七爷来了！这里是七筒，很高兴为您服务。\n\n在使用本 bot 之前，请仔细阅读下方的免责协议，如有任何问题请与开发者联系。";
    public static final String DISCLAIMER = "本项目仅限学习使用，不涉及到任何商业或者金钱用途，禁止用于非法行为。您的使用行为将被视为对本声明全部内容的认可。本声明在您邀请该账号（QQ账号：340865180）进入任何腾讯QQ群聊时生效。\n" +
            "\n" +
            "本项目在运作时，不可避免地会使用到您的QQ号、QQ昵称、群号、群昵称等信息。后台不会收集具体的聊天内容，如果您对此有所疑问，请停止使用本项目。基于维持互联网秩序的考量，请勿恶意使用本项目。本项目有权停止对任何对象的服务，任何解释权均归本项目开发组所有。\n" +
            "\n" +
            "本项目涉及或使用到的开源项目有：基于 AGPLv3 协议的 Mirai (https://github.com/mamoe/mirai) ，基于 Apache License 2.0 协议的谷歌 Gson (https://github.com/google/gson) ，清华大学开放中文词库 (http://thuocl.thunlp.org/) ，动物图片来自互联网开源动物图片API Shibe.online(shibes as a service)、Dog.ceo (The internet's biggest collection of open source dog pictures.)、random.dog (Hello World, This Is Dog)。\n";

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 决定是否接收加群邀请
    public static void handleGroupInvitation(BotInvitedJoinGroupRequestEvent event) {
        if(IdentityUtil.isAdmin(Objects.requireNonNull(event.getInvitor()).getId())) {
            event.accept();
            return;
        }
        if (!MultiBotHandler.canAcceptGroup(event.getBot().getId())) {
            if(MultiBotHandler.canSendNotice(event.getBot())) {
                event.getInvitor().sendMessage(MultiBotHandler.rejectInformation(event.getBot().getId()));
            }
            event.ignore();
        } else {
            event.accept();
        }

    }

    // 决定是否接收好友请求
    public static void handleFriendRequest(NewFriendRequestEvent event) {
        if(IdentityUtil.isAdmin(event.getFromId())) event.accept();
        if (!MultiBotHandler.canAcceptFriend(event.getBot().getId())) {
            event.reject(false);
            return;
        }
        event.accept();
    }

    // 处理加群事件
    public static void handleJoinGroup(BotJoinGroupEvent.Invite event) {
        if(IdentityUtil.DevGroup.DEFAULT.isDevGroup(event.getGroupId())){
            event.getGroup().sendMessage("反正是开发组，咱没话说。");
            return;
        }
        executor.schedule(() -> {
            //管理员判定
            if(!IdentityUtil.isAdmin((event).getInvitor().getId())) {
                if (!MultiBotHandler.canAcceptGroup(event.getBot().getId())) {
                    event.getGroup().sendMessage(MultiBotHandler.rejectInformation(event.getBot().getId()));
                    if(MultiBotHandler.canSendNotice(event.getBot())) {
                        (event).getInvitor().sendMessage(MultiBotHandler.rejectInformation(event.getBot().getId()));
                    }
                    event.getGroup().quit();
                    String content = "由于目前Bot不接受添加群聊，已经从 " + event.getGroup().getName() + "(" + event.getGroup().getId() + ")" + "出逃。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }

                if (event.getGroup().getMembers().getSize() < 7) {
                    event.getGroup().sendMessage("七筒目前不接受加入7人以下的群聊，将会自动退群。");
                    if(MultiBotHandler.canSendNotice(event.getBot())) {
                        (event).getInvitor().sendMessage("七筒目前不接受加入7人以下的群聊，将会自动退群。");
                    }
                    event.getGroup().quit();
                    String content = (event).getInvitor().getNick() + "(" + (event).getInvitor().getId() + ")尝试邀请七筒加入一个少于7人的群聊。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }
            }

            //检测是否有其他七筒
            for(NormalMember nm:event.getGroup().getMembers()){
                for(Bot bot:Bot.getInstances()){
                    if (bot.getId()==(event.getBot().getId())) continue;
                    if (nm.getId()==bot.getId()){
                        if(MultiBotHandler.BotName.get(nm.getId()).ordinal()<MultiBotHandler.BotName.get(bot.getId()).ordinal()) continue;//很重要的判定！两个七筒只有一个退群！
                        event.getGroup().sendMessage("检测到其他在线七筒账户在此群聊中，本机器人将自动退群。");
                        executor.schedule(() -> event.getGroup().quit(),15,TimeUnit.SECONDS);
                        return;
                    }
                }
            }

            // 正常通过群邀请加群
            sendNoticeWhenJoinGroup(event.getGroup(),IdentityUtil.containsUnusedBot(event.getGroup()),event.getBot());
            notifyDevWhenJoinGroup(event);

            event.getGroup().sendMessage(JOIN_GROUP);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DisclTemporary.send(event.getGroup());
        },10,TimeUnit.SECONDS);

    }

    // 处理加群事件
    public static void handleJoinGroup(BotJoinGroupEvent.Active event) {
        if(IdentityUtil.DevGroup.DEFAULT.isDevGroup(event.getGroupId())){
            event.getGroup().sendMessage("反正是开发组，咱没话说。");
            return;
        }
        executor.schedule(() -> {

                if (!MultiBotHandler.canAcceptGroup(event.getBot().getId())) {
                    event.getGroup().sendMessage(MultiBotHandler.rejectInformation(event.getBot().getId()));
                    event.getGroup().quit();
                    String content = "由于目前Bot不接受添加群聊，已经从 " + event.getGroup().getName() + "(" + event.getGroup().getId() + ")" + "出逃。";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }

                if (event.getGroup().getMembers().getSize() < 7) {
                    event.getGroup().sendMessage("七筒目前不接受加入7人以下的群聊。");
                    event.getGroup().quit();
                    String content = "有人尝试尝试邀请七筒加入一个少于7人的群聊 "+event.getGroup().getName()+"("+event.getGroup().getId()+")，七筒已经离开";
                    MessageUtil.notifyDevGroup(content, event.getBot().getId());
                    return;
                }


            //检测是否有其他七筒
            for(NormalMember nm:event.getGroup().getMembers()){
                for(Bot bot:Bot.getInstances()){
                    if (bot.getId()==(event.getBot().getId())) continue;
                    if (nm.getId()==bot.getId()){
                        if(MultiBotHandler.BotName.get(nm.getId()).ordinal()<MultiBotHandler.BotName.get(bot.getId()).ordinal()) continue;//很重要的判定！两个七筒只有一个退群！
                        event.getGroup().sendMessage("检测到其他在线七筒账户在此群聊中，本机器人将自动退群。");
                        executor.schedule(() -> event.getGroup().quit(),15,TimeUnit.SECONDS);
                        return;
                    }
                }
            }

            // 正常通过群邀请加群
            sendNoticeWhenJoinGroup(event.getGroup(),IdentityUtil.containsUnusedBot(event.getGroup()),event.getBot());
            notifyDevWhenJoinGroup(event);

            event.getGroup().sendMessage(JOIN_GROUP);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DisclTemporary.send(event.getGroup());
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
        if(!MultiBotHandler.canSendNotice(event.getBot())) return;
        executor.schedule(() -> {
            event.getFriend().sendMessage(JOIN_GROUP);
            DisclTemporary.send(event.getFriend());
        },15, TimeUnit.SECONDS);
    }

    /**
     * 尝试退出某个群
     */
    public static void tryQuitGroup(long id) {
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Group group = bot.getGroup(id);
            //  TODO 问题来了，退群要给群内发通知吗?
            if (group != null) group.quit();
        }
    }

    /**
     * 尝试删除好友
     */
    public static void tryDeleteFriend(long id) {
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Friend friend = bot.getFriend(id);
            //  TODO 问题来了，删除好友需要告知被删除对象吗？
            if (friend != null) friend.delete();
        }
    }

    // 加群后发送Bot提示
    static void sendNoticeWhenJoinGroup(BotJoinGroupEvent.Active event,boolean containsOldChitung) {
        if(!MultiBotHandler.canSendNotice(event.getBot())) return;
        String message = "您好，七筒已经加入了您的群" + event.getGroup().getName() + " - " + event.getGroup().getId() + "，请在群聊中输入/help 以获取相关信息。如果七筒过于干扰群内秩序，请将七筒从您的群中移除。";
        if(containsOldChitung) message+="\n\n检测到您的群聊中有已经不再投入使用的七筒账号，可以移除。";
        event.getGroup().getOwner().sendMessage(message);
    }

    static void sendNoticeWhenJoinGroup(Group group,boolean containsOldChitung, Bot bot) {
        if(!MultiBotHandler.canSendNotice(bot)) return;
        String message = "您好，七筒已经加入了您的群" + group.getName() + " - " + group.getId() + "，请在群聊中输入/help 以获取相关信息。如果七筒过于干扰群内秩序，请将七筒从您的群中移除。";
        if(containsOldChitung) message+="\n\n检测到您的群聊中有已经不再投入使用的七筒账号，可以移除。";
        group.getOwner().sendMessage(message);
    }

    // 向开发者发送加群提醒
    static void notifyDevWhenJoinGroup(BotJoinGroupEvent.Invite event) {
        MessageUtil.notifyDevGroup("七筒已加入 " + event.getGroup().getName() + "（" + event.getGroupId() + "）,邀请人为 "
                + ((BotJoinGroupEvent.Invite) event).getInvitor().getNick() + "（" + ((BotJoinGroupEvent.Invite) event).getInvitor().getId() + "）。", event.getBot().getId());
    }

    // 向开发者发送加群提醒
    static void notifyDevWhenJoinGroup(BotJoinGroupEvent.Active event) {
        MessageUtil.notifyDevGroup("七筒已加入 " + event.getGroup().getName() + "（" + event.getGroupId() + "）", event.getBot().getId());
    }

    // 向开发者发送退群提醒
    static void notifyDevWhenLeaveGroup(BotLeaveEvent.Kick event) {
        //todo:Mirai开发者告知这个写法可能不稳定
        MessageUtil.notifyDevGroup("七筒已经从 " + event.getGroup().getName() + "（" + event.getGroupId() + "）离开，由管理员操作。", event.getBot().getId());
    }

    // 向开发者发送退群提醒
    static void notifyDevWhenLeaveGroup(BotLeaveEvent.Active event) {
        //todo:Mirai开发者告知这个写法可能不稳定
        MessageUtil.notifyDevGroup("七筒已经从 " + event.getGroup().getName() + "（" + event.getGroupId() + "）主动离开。", event.getBot().getId());
    }

    enum JoinGroupSourceType {
        INVITE,
        RETRIEVE,
        ACTIVE
    }
}
