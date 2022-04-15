package lielietea.mirai.plugin.core.broadcast;


import io.ktor.http.auth.HttpAuthHeader;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.BlackJack;
import lielietea.mirai.plugin.utils.IdentityUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BroadcastSystem {

    enum BroadcastType{
        Friend,
        Group
    }

    static class AdminContact{
        Contact contact;
        Long adminID;
        AdminContact(Contact contact,Long adminID){
            this.contact = contact;
            this.adminID = adminID;
        }
    }

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Map<AdminContact,BroadcastType> BroadcastModeList = new HashMap<>();

    BroadcastSystem() {}
    private static final BroadcastSystem INSTANCE;
    static { INSTANCE = new BroadcastSystem(); }

    public static BroadcastSystem getINSTANCE() {
        return INSTANCE;
    }

    public static void handle(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        broadcast(event);
    }

    static BroadcastType getBT(MessageEvent event){
        for(AdminContact ac:getINSTANCE().BroadcastModeList.keySet()){
            if(ac.adminID==event.getSender().getId()){
                return getINSTANCE().BroadcastModeList.get(ac);
            }
        }
        return null;
    }

    static boolean isInBroadcastMode(MessageEvent event){
        for(AdminContact adminContact:getINSTANCE().BroadcastModeList.keySet()){
            if(adminContact.contact.equals(event.getSubject())&&adminContact.adminID==event.getSender().getId()) return true;
        }
        return false;
    }

    static void quitBroadcastMode(MessageEvent event){
        Set<AdminContact> clonedKeySet = getINSTANCE().BroadcastModeList.keySet();
        for(AdminContact adminContact:clonedKeySet){
            if(adminContact.contact.equals(event.getSubject())&&adminContact.adminID==event.getSender().getId()) {
                getINSTANCE().BroadcastModeList.remove(adminContact);
                return;
            }
        }
    }

    static void broadcast(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;

        //在广播模式中
        if(isInBroadcastMode(event)){

            if(!checkMessage(event)){
                event.getSubject().sendMessage("暂不支持特定消息类型的发送，请重试。已退出广播模式。");
                quitBroadcastMode(event);
                return;
            }

            BroadcastType bt = getBT(event);

            if(bt==null){
                event.getSubject().sendMessage("请检查是否进入广播模式。");
                quitBroadcastMode(event);
                return;
            }

            switch(bt){
                case Group:
                    executor.schedule(new SendMessage<>(event.getBot().getGroups(), event.getMessage(),new AdminContact(event.getSubject(), event.getSender().getId())),1,TimeUnit.SECONDS);
                    break;
                case Friend:
                    executor.schedule(new SendMessage<>(event.getBot().getFriends(),event.getMessage(),new AdminContact(event.getSubject(), event.getSender().getId())),1,TimeUnit.SECONDS);
                    break;
            }

        } else {

            if(event.getMessage().contentToString().toLowerCase().contains("/broadcast")){
                enterBroadcastMode(event);
            }

        }

    }

    static boolean checkMessage(MessageEvent event){
        MessageChain rawMessage = event.getMessage();
        for(SingleMessage sm:rawMessage){
            if (!(sm instanceof Image)&&!(sm instanceof PlainText)) {
                return false;
            }
        }
        return true;
    }

    static MessageChain addAt(Group group,SingleMessage message){
        MessageChainBuilder result = new MessageChainBuilder();
        String[] splitMessage = message.contentToString().split("@");
        for(String s:splitMessage){
            if(s.startsWith("Admin")||s.startsWith("admin")){
                for(NormalMember nm:group.getMembers()){
                    if(nm.getPermission().equals(MemberPermission.ADMINISTRATOR)||nm.getPermission().equals(MemberPermission.OWNER)){
                        result.add(new At(nm.getId()));
                    }
                }
                s = s.replaceFirst("Admin|admin","");
            }

            if(s.startsWith("Owner")||s.startsWith("owner")){
                result.add(new At(group.getOwner().getId()));
                s = s.replaceFirst("Owner|owner","");
            }

            result.add(s);
        }
        return result.asMessageChain();
    }

    static MessageChain processAt(Group group, MessageChain rawMessage){
        MessageChainBuilder result = new MessageChainBuilder();
        for(SingleMessage sm:rawMessage){
            if(sm instanceof PlainText){
                if(sm.contentToString().toLowerCase().contains("@admin")||sm.contentToString().toLowerCase().contains("@owner")){
                    result.add(addAt(group,sm));
                    continue;
                }
            }
            result.add(sm);
        }
        return result.asMessageChain();
    }

    static void enterBroadcastMode(MessageEvent event){

        String rawString = event.getMessage().contentToString().toLowerCase().replace("/broadcast", "").replace(" ", "").replace("-", "");

        switch (rawString) {
            case "f":
            case "friend":
                getINSTANCE().BroadcastModeList.put(new AdminContact(event.getSubject(), event.getSender().getId()), BroadcastType.Friend);
                break;
            case "g":
            case "group":
                getINSTANCE().BroadcastModeList.put(new AdminContact(event.getSubject(), event.getSender().getId()), BroadcastType.Group);
                break;
            default:
                event.getSubject().sendMessage("广播功能指示器使用错误，请输入/broadcast 加 -f 或者 -g 进入广播模式。");
                return;
        }

        event.getSubject().sendMessage("已进入广播模式。请注意，您在该消息窗口内发送的下一条消息将进行广播。可以在消息中加入 @owner @admin 来提及群主或者管理员。");

        //90秒内关闭窗口模式以防止误触
        executor.schedule(new quitBroadcastModeAutomatically(event),90, TimeUnit.SECONDS);

    }

    static class quitBroadcastModeAutomatically implements Runnable{

        MessageEvent event;

        public quitBroadcastModeAutomatically(MessageEvent event){
            this.event = event;
        }

        @Override
        public void run(){
            if(isInBroadcastMode(event)) {
                quitBroadcastMode(event);
                event.getSubject().sendMessage("广播模式已经自动关闭。");
            }
        }
    }

    static class SendMessage<T extends Contact> implements Runnable{

        ContactList<T> contactList;
        MessageChain mc;
        AdminContact ac;

        public SendMessage(ContactList<T> contactList,MessageChain mc,AdminContact ac){
            this.contactList=contactList;
            this.mc=mc;
            this.ac=ac;
        }

        @Override
        public void run(){

            boolean isGroup = false;
            boolean hasJudged = false;

            for(Contact c:contactList){

                if(!hasJudged){
                    if(c instanceof Group) isGroup=true;
                    hasJudged = true;
                }

                if(c instanceof Group){
                    mc = processAt((Group) c,mc);
                }

                c.sendMessage(mc);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            String notification = "已经成功通知所有";

            if(isGroup){
                notification+="群聊。";
            } else {
                notification+="好友。";
            }

            ac.contact.sendMessage(notification);

        }
    }


}
