package mirai.chitung.plugin;


import mirai.chitung.plugin.administration.AdminCommandDispatcher;
import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.game.fish.Fishing;
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager;
import mirai.chitung.plugin.core.harbor.Harbor;
import mirai.chitung.plugin.core.responder.Blacklist;
import mirai.chitung.plugin.core.responder.ResponderCenter;
import mirai.chitung.plugin.core.responder.repeater.Repeater;
import mirai.chitung.plugin.core.responder.help.NewHelp;
import mirai.chitung.plugin.core.responder.imageresponder.ImageResponder;
import mirai.chitung.plugin.core.responder.universalrespond.URManager;
import mirai.chitung.plugin.utils.*;
import mirai.chitung.plugin.core.broadcast.BroadcastSystem;
import mirai.chitung.plugin.core.game.GameCenter;
import mirai.chitung.plugin.core.responder.ResponderManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;

import java.util.Objects;

/*
使用java请把
src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin
文件内容改成"org.example.mirai.plugin.JavaPluginMain"也就是当前主类
使用java可以把kotlin文件夹删除不会对项目有影响

在settings.gradle.kts里改生成的插件.jar名称
build.gradle.kts里改依赖库和插件版本
在主类下的JvmPluginDescription改插件名称，id和版本
用runmiraikt这个配置可以在ide里运行，不用复制到mcl或其他启动器
 */

public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();

    private JavaPluginMain() {
        super(new JvmPluginDescriptionBuilder("mirai.chitung-public", "0.1.0")
                .info("Open version of Chitung")
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("日志");

        InitializeUtil.initialize();

        // 上线事件
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, event -> {

            if(ConfigHandler.getINSTANCE().config.getCc().getNudgeText().equals("")) return;

            for(Long groupID: IdentityUtil.getDevGroup()){
                if(event.getBot().getGroup(groupID)!=null) {
                    Objects.requireNonNull(event.getBot().getGroup(groupID)).sendMessage(ConfigHandler.getINSTANCE().config.getCc().getOnlineText());
                }
            }
            //更新群设置列表
            GroupConfigManager.updateConfigList();
        });

        // 处理好友请求
        GlobalEventChannel.INSTANCE.subscribeAlways(NewFriendRequestEvent.class, ContactUtil::handleFriendRequest);

        // 加为好友之后发送简介与免责声明
        GlobalEventChannel.INSTANCE.subscribeAlways(FriendAddEvent.class, ContactUtil::handleAddFriend);

        // 处理拉群请求
        GlobalEventChannel.INSTANCE.subscribeAlways(BotInvitedJoinGroupRequestEvent.class, ContactUtil::handleGroupInvitation);

        // 加群后处理加群事件
        GlobalEventChannel.INSTANCE.subscribeAlways(BotJoinGroupEvent.Invite.class, ContactUtil::handleJoinGroup);

        // 加群后处理加群事件
        GlobalEventChannel.INSTANCE.subscribeAlways(BotJoinGroupEvent.Active.class, ContactUtil::handleJoinGroup);

        // Bot被踢
        GlobalEventChannel.INSTANCE.subscribeAlways(BotLeaveEvent.Kick.class, ContactUtil::handleLeaveGroup);

        // Bot离群
        GlobalEventChannel.INSTANCE.subscribeAlways(BotLeaveEvent.Active.class, ContactUtil::handleLeaveGroup);

        // Bot获得权限
        GlobalEventChannel.INSTANCE.subscribeAlways(BotGroupPermissionChangeEvent.class, event -> {
            if (event.getGroup().getBotPermission().equals(MemberPermission.OWNER) || (event.getGroup().getBotPermission().equals(MemberPermission.ADMINISTRATOR))) {
                event.getGroup().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getPermissionChangedText());
            }
        });

        // 群名改变之后发送消息
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupNameChangeEvent.class, event -> event.getGroup().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getGroupNameChangedText()));

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, event -> {

            //管理员功能
            AdminCommandDispatcher.getInstance().handleMessage(event);
            //广播
            BroadcastSystem.handle(event);

            if(!ConfigHandler.canAnswerGroup()) return;

            if(IdentityUtil.isBot(event)) return;

            if(!GroupConfigManager.globalConfig(event)) return;
            if(GroupConfigManager.isBlockedUser(event)) return;

            if(Blacklist.isBlocked(event.getSender().getId(), Blacklist.BlockKind.Friend)) return;
            if(Blacklist.isBlocked(event.getGroup().getId(), Blacklist.BlockKind.Group)) return;

            //GameCenter
            GameCenter.handle(event);

            if(Harbor.isReachingPortLimit(event)) return;

            //ResponderCenter
            if(GroupConfigManager.responderConfig(event) && ConfigHandler.getINSTANCE().config.getGroupFC().isResponder()){

                NewHelp.handle(event);
                Nudge.mentionNudge(event);
                ResponderCenter.getINSTANCE().handleMessage(event);
                ImageResponder.handle(event);
                Repeater.handle(event);

                //钓鱼
                Fishing.go(event);
                //Universal Responder
                URManager.handle(event);
                //群管理功能
                GroupConfigManager.handle(event);
            }

        });

        //被人戳一戳了
        GlobalEventChannel.INSTANCE.subscribeAlways(NudgeEvent.class, Nudge::returnNudge);

        //群成员入群自动欢迎
        GlobalEventChannel.INSTANCE.subscribeAlways(MemberJoinEvent.class, memberJoinEvent -> memberJoinEvent.getGroup().sendMessage(ConfigHandler.getINSTANCE().config.getCc().getWelcomeText()));


        //计数
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, event -> {return;});
        GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessagePostSendEvent.class, event -> {return;});

        //临时消息
        GlobalEventChannel.INSTANCE.subscribeAlways(StrangerMessageEvent.class, event -> {return;});
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupTempMessageEvent.class, event -> {return;});

        //好友消息
        GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, event -> {

            if(!ConfigHandler.canAnswerFriend()) return;
            if(IdentityUtil.isBot(event)) return;

            if(Blacklist.isBlocked(event.getSender().getId(), Blacklist.BlockKind.Friend)) return;

            //管理员功能
            AdminCommandDispatcher.getInstance().handleMessage(event);
            //GameCenter
            GameCenter.handle(event);
            //广播
            BroadcastSystem.handle(event);

            if(Harbor.isReachingPortLimit(event)) return;

            //ResponderCenter
            if(ConfigHandler.getINSTANCE().config.getFriendFC().isResponder()) {
                NewHelp.handle(event);
                ResponderCenter.getINSTANCE().handleMessage(event);
                URManager.handle(event);
                ImageResponder.handle(event);
                Fishing.go(event);
            }

        });
    }

    @Override
    public void onDisable() {
        ResponderManager.getINSTANCE().close();
        ResponderCenter.getINSTANCE().close();
        AdminCommandDispatcher.getInstance().close();
    }
}