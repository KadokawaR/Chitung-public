package mirai.chitung.plugin.core.groupconfig;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.harbor.Harbor;
import mirai.chitung.plugin.utils.IdentityUtil;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GroupConfigManager {

    static String GC_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator +"GroupConfig.json";
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    GroupConfigManager(){}

    private static final GroupConfigManager INSTANCE;

    static class GroupConfigs{
        List<GroupConfig> groupConfigList;
        GroupConfigs(){
            this.groupConfigList = new ArrayList<>();
        }
    }

    static {
        INSTANCE = new GroupConfigManager();
        initialize();
    }

    GroupConfigs groupConfigs;

    public static GroupConfigManager getINSTANCE() {
        return INSTANCE;
    }

    public static void initialize(){
        getINSTANCE().groupConfigs = new GroupConfigs();
        if(Touch.file(GC_PATH)){
            try {
                getINSTANCE().groupConfigs = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(GC_PATH)), StandardCharsets.UTF_8))), GroupConfigs.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    static void readRecord(){
        try {
            getINSTANCE().groupConfigs = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(GC_PATH)), StandardCharsets.UTF_8))), GroupConfigs.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().groupConfigs);
        Write.cover(jsonString, GC_PATH);
    }

    static Integer getGroupIndex(long groupID){

        for(int i=0;i<getINSTANCE().groupConfigs.groupConfigList.size();i++){
            if(getINSTANCE().groupConfigs.groupConfigList.get(i).getGroupID()==groupID) return i;
        }

        addGroupConfig(groupID);

        for(int i=0;i<getINSTANCE().groupConfigs.groupConfigList.size();i++){
            if(getINSTANCE().groupConfigs.groupConfigList.get(i).getGroupID()==groupID) return i;
        }

        return null;
    }

    static boolean containsGroup(long groupID){
        return getGroupIndex(groupID)!=null;
    }

    static void addGroupConfig(long groupID){

        for(int i=0;i<getINSTANCE().groupConfigs.groupConfigList.size();i++){
            if(getINSTANCE().groupConfigs.groupConfigList.get(i).getGroupID()==groupID) return;
        }

        getINSTANCE().groupConfigs.groupConfigList.add(new GroupConfig(groupID));
        writeRecord();
        readRecord();
    }

    static void addGroupConfig(List<Long> groupID){
        for(Long ID:groupID){
            if(!containsGroup(ID)) getINSTANCE().groupConfigs.groupConfigList.add(new GroupConfig(ID));
        }
        writeRecord();
        readRecord();
    }

    public static void deleteGroupConfig(long groupID){
        if(containsGroup(groupID)){
            getINSTANCE().groupConfigs.groupConfigList.remove(Objects.requireNonNull(getGroupIndex(groupID)).intValue());
        }
        writeRecord();
        readRecord();
    }

    static void deleteGroupConfig(List<Long> groupID){
        for(Long ID:groupID){
            if(containsGroup(ID)){
                getINSTANCE().groupConfigs.groupConfigList.remove(Objects.requireNonNull(getGroupIndex(ID)).intValue());
            }
        }
        writeRecord();
        readRecord();
    }

    public static void updateConfigList(){
        List<Long> mutualList = new ArrayList<>();
        List<Long> botGroupList = new ArrayList<>();
        List<Long> configGroupList = new ArrayList<>();
        List<Long> addList = new ArrayList<>();
        List<Long> deleteList = new ArrayList<>();
        List<Group> groupList = new ArrayList<>();

        for(Bot bot:Bot.getInstances()){
            groupList.addAll(bot.getGroups());
        }

        for(Group g:groupList){
            botGroupList.add(g.getId());
        }

        for(GroupConfig gc:getINSTANCE().groupConfigs.groupConfigList){
            configGroupList.add(gc.getGroupID());
        }

        for(GroupConfig gc:getINSTANCE().groupConfigs.groupConfigList){
            for(Long g:botGroupList){
                if(g==gc.getGroupID()) mutualList.add(gc.getGroupID());
            }
        }

        for(Long g:botGroupList){
            if(!mutualList.contains(g)) addList.add(g);
        }

        for(Long g:configGroupList){
            if(!mutualList.contains(g)) deleteList.add(g);
        }

        addGroupConfig(addList);
        deleteGroupConfig(deleteList);

    }

    static GroupConfig readGroupConfig(long groupID){
        if(getGroupIndex(groupID)==null) addGroupConfig(groupID);
        return getINSTANCE().groupConfigs.groupConfigList.get(Objects.requireNonNull(getGroupIndex(groupID)));
    }

    @SuppressWarnings("RedundantIfStatement")
    public static void changeGroupConfig(GroupMessageEvent event,String message){

        if(!message.startsWith("/close")&&!message.startsWith("/open")) return;
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&(!IdentityUtil.isAdmin(event))) return;
        if(!containsGroup(event.getGroup().getId())) addGroupConfig(event.getGroup().getId());

        boolean operation;

        if(message.startsWith("/close")){
            operation = false;
        } else {
            operation = true;
        }

        message = message.replace("/close","").replace("/open","").replace(" ","");

        Integer index = getGroupIndex(event.getGroup().getId());
        assert index!=null;

        switch(message){
            case "global":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setGlobal(operation);
                event.getSubject().sendMessage("已设置全局消息的响应状态为"+operation);
                break;
            case "fish":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setFish(operation);
                event.getSubject().sendMessage("已设置钓鱼的响应状态为"+operation);
                break;
            case "casino":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setCasino(operation);
                event.getSubject().sendMessage("已设置娱乐游戏的响应状态为"+operation);
                break;
            case "responder":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setResponder(operation);
                event.getSubject().sendMessage("已设置关键词触发功能的响应状态为"+operation);
                break;
            case "game":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setGame(operation);
                event.getSubject().sendMessage("已设置所有游戏的响应状态为"+operation);
                break;
            case "lottery":
                getINSTANCE().groupConfigs.groupConfigList.get(index).setLottery(operation);
                event.getSubject().sendMessage("已设置C4和Bummer功能的响应状态为"+operation);
                break;
            default:
                event.getSubject().sendMessage("群设置指示词使用错误，使用/close或者/open加上空格加上global、game、casino、responder、fish或者lottery来开关相应内容。");
                Harbor.count(event);
                return;
        }

        Harbor.count(event);

        writeRecord();
        readRecord();

    }

    public static void resetGroupConfig(GroupMessageEvent event,String message){
        if(!message.equalsIgnoreCase("/default")) return;
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&(!IdentityUtil.isAdmin(event))) return;
        if(containsGroup(event.getGroup().getId())){
            getINSTANCE().groupConfigs.groupConfigList.remove(Objects.requireNonNull(getGroupIndex(event.getGroup().getId())).intValue());
        }
        addGroupConfig(event.getGroup().getId());
    }

    public static boolean globalConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        if(!getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isGlobal()){
            if(IdentityUtil.isAdmin(event)) return true;
            return !event.getSender().getPermission().equals(MemberPermission.MEMBER);
        }
        return true;
    }

    public static boolean fishConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isFish();
    }

    public static boolean casinoConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isCasino();
    }

    public static boolean responderConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isResponder();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean lotteryConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isLottery();
    }

    public static boolean gameConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isGame();
    }

    static void addBlockedUser(GroupMessageEvent event,String message){
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&!IdentityUtil.isAdmin(event)) return;
        if(!message.startsWith("/blockmember")) return;

        executor.schedule(new BlockUserAndSendNotice(event),1, TimeUnit.SECONDS);

        Harbor.count(event);

    }

    static void deleteBlockedUser(GroupMessageEvent event,String message){
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&!IdentityUtil.isAdmin(event)) return;
        if(!message.startsWith("/unblockmember")) return;

        List<Long> deletedUser = new ArrayList<>();

        for(SingleMessage sm:event.getMessage()){

            String SMM = sm.contentToString();
            if(SMM.startsWith("@")){

                Long ID = Long.parseLong(SMM.replace("@",""));

                if(isBlockedUser(event.getGroup().getId(),ID)){
                    getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).getBlockedUser().remove(ID);
                    deletedUser.add(ID);
                }
            }
        }

        sendMessage(event,false,deletedUser);

        Harbor.count(event);

    }

    static void sendMessage(GroupMessageEvent event,boolean isBlocking, List<Long> userList){
        MessageChainBuilder mcb = new MessageChainBuilder();

        if(isBlocking){
            mcb.append("已经在本群中屏蔽：");
        } else {
            mcb.append("已经在本群中解除屏蔽：");
        }

        for(Long ID:userList) { mcb.append(new At(ID)); }

        event.getSubject().sendMessage(mcb.asMessageChain());

    }

    static class BlockUserAndSendNotice implements Runnable{

        private final GroupMessageEvent event;

        BlockUserAndSendNotice(GroupMessageEvent event){
            this.event=event;
        }

        @Override
        public void run(){

            List<Long> blockedNormalMember = new ArrayList<>();
            List<Long> blockedGroupAdminMember = new ArrayList<>();
            List<Long> blockedAdminMember = new ArrayList<>();

            for(SingleMessage sm:event.getMessage()){

                String SMM = sm.contentToString();

                if(SMM.startsWith("@")){

                    Long ID = Long.parseLong(SMM.replace("@",""));

                    if(event.getGroup().getOrFail(ID).getPermission().equals(MemberPermission.OWNER)||event.getGroup().getOrFail(ID).getPermission().equals(MemberPermission.ADMINISTRATOR)){
                        blockedGroupAdminMember.add(ID);
                        continue;
                    }

                    if(IdentityUtil.isAdmin(ID)){
                        blockedAdminMember.add(ID);
                        continue;
                    }

                    if(event.getGroup().getMembers().contains(ID.longValue())){
                        getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).getBlockedUser().add(ID);
                        blockedNormalMember.add(ID);
                    }
                }
            }

            if(blockedGroupAdminMember.size()>0){
                MessageChainBuilder mcb = new MessageChainBuilder().append("拉黑同为管理员的");
                for(Long ID:blockedGroupAdminMember){
                    mcb.append(new At(ID));
                }
                mcb.append("？不要把").append(ConfigHandler.getName(event.getBot())).append("卷入勾心斗角的宫廷争斗！");
                event.getSubject().sendMessage(mcb.asMessageChain());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(blockedAdminMember.size()>0){
                MessageChainBuilder mcb = new MessageChainBuilder().append("抱歉，").append(ConfigHandler.getName(event.getBot())).append("的运营者");
                for(Long ID:blockedAdminMember){
                    mcb.append(new At(ID));
                }
                mcb.append("就是可以为所欲为。");
                event.getSubject().sendMessage(mcb.asMessageChain());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(blockedNormalMember.size()>0){
                sendMessage(event,true,blockedNormalMember);
            }

        }
    }

    public static boolean isBlockedUser(GroupMessageEvent event){
        if(event.getSender().getPermission().equals(MemberPermission.OWNER)||event.getSender().getPermission().equals(MemberPermission.ADMINISTRATOR)) return false;
        if(IdentityUtil.isAdmin(event.getSender().getId())) return false;
        return readGroupConfig(event.getGroup().getId()).getBlockedUser().contains(event.getSender().getId());
    }

    public static boolean isBlockedUser(long groupID, long memberID){
        return readGroupConfig(groupID).getBlockedUser().contains(memberID);
    }

    public static GroupConfig getGroupConfig(long groupID){
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(groupID));
    }

    static void groupConfigCheck(GroupMessageEvent event, String message){
        if(message.equalsIgnoreCase("/groupconfig -c")||message.equalsIgnoreCase("/gc -c")||message.equals("查看群设置")) {
            MessageChainBuilder mcb = new MessageChainBuilder();
            GroupConfig gc = getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId()));
            mcb.append("本群的群设置状态为：")
                    .append("\n全局：").append(String.valueOf(gc.isGlobal()))
                    .append("\n关键词触发功能：").append(String.valueOf(gc.isResponder()))
                    .append("\n游戏功能：").append(String.valueOf(gc.isGame()))
                    .append("\nCasino功能：").append(String.valueOf(gc.isCasino()))
                    .append("\n钓鱼功能：").append(String.valueOf(gc.isFish()))
                    .append("\n群内抽奖功能：").append(String.valueOf(gc.isLottery()));
            event.getSubject().sendMessage(mcb.asMessageChain());
        }
    }

    static void groupConfigHelp(GroupMessageEvent event, String message){
        if(message.equalsIgnoreCase("/groupconfig -h")||message.equalsIgnoreCase("/gc -h")||message.equals("群设置帮助")) {
            event.getSubject().sendMessage("使用/close或者/open加上空格加上global（全局）、game（游戏）、casino、responder（关键词触发）、fish（钓鱼）或者lottery（群内抽奖）来开关相应内容。\n" +
                    "使用/default恢复所有初始设置。" +
                    "使用/blockmember并@成员来屏蔽该成员，使用/unblockmember并@成员来解除屏蔽。" +
                    "使用/blocklist查询被屏蔽的用户列表，如果不需要返回结果@用户请使用/blocklist -m。");
        }
    }

    static void getBlockList(GroupMessageEvent event, String message){

        if(message.toLowerCase().startsWith("/blocklist -m")){
            StringBuilder sb = new StringBuilder();
            sb.append("本群的屏蔽名单有：");
            for(long ID:getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).getBlockedUser()){
                if(event.getGroup().getMembers().contains(ID)){
                    sb.append(ID).append(" ");
                }

            }
            event.getSubject().sendMessage(sb.toString().trim());
            return;
        }

        if(message.equalsIgnoreCase("/blocklist")){

            List<Long> blocklist = getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).getBlockedUser();
            if(blocklist.size()==0){
                event.getGroup().sendMessage("本群暂无被屏蔽成员。");
                return;
            }
            MessageChainBuilder mcb = new MessageChainBuilder();
            mcb.append("本群的屏蔽名单有：");
            for(long ID:blocklist){
                if(event.getGroup().getMembers().contains(ID)){
                    mcb.append(new At(ID));
                }
            }
            event.getSubject().sendMessage(mcb.asMessageChain());
        }
    }

    public static void handle(GroupMessageEvent event){
        if(event.getSender().getPermission()==MemberPermission.MEMBER&&!IdentityUtil.isAdmin(event)) return;
        String message = event.getMessage().contentToString().toLowerCase();
        resetGroupConfig(event,message);
        changeGroupConfig(event,message);
        addBlockedUser(event,message);
        deleteBlockedUser(event,message);
        groupConfigCheck(event,message);
        getBlockList(event,message);
        groupConfigHelp(event,message);
    }

    public void ini(){
        System.out.println("Initialize Group Config Manager");
    }

}
