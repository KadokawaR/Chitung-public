package mirai.chitung.plugin.core.groupconfig;

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
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class GroupConfigManager {
    static String GC_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator +"GroupConfig.json";

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
                getINSTANCE().groupConfigs = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(GC_PATH)))), GroupConfigs.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
        updateConfigList();
    }

    static void readRecord(){
        try {
            getINSTANCE().groupConfigs = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(GC_PATH)))), GroupConfigs.class);
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
        return null;
    }

    static boolean containsGroup(long groupID){
        return getGroupIndex(groupID)!=null;
    }

    static void addGroupConfig(long groupID){
        if(containsGroup(groupID)) return;
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

    static void deleteGroupConfig(long groupID){
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

     static void updateConfigList(){
        List<Long> mutualList = new ArrayList<>();
        List<Long> botGroupList = new ArrayList<>();
        List<Long> configGroupList = new ArrayList<>();
        List<Long> addList = new ArrayList<>();
        List<Long> deleteList = new ArrayList<>();
        ContactList<Group> groupList = new ContactList<>();

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
    public static void changeGroupConfig(GroupMessageEvent event){
        if(!event.getMessage().contentToString().contains("/close")||event.getMessage().contentToString().contains("/open")) return;
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&(!IdentityUtil.isAdmin(event))) return;
        if(!containsGroup(event.getGroup().getId())) addGroupConfig(event.getGroup().getId());
        boolean operation;
        if(event.getMessage().contentToString().contains("/close")){
            operation = false;
        } else {
            operation = true;
        }
        String message = event.getMessage().contentToString();
        message = message.toLowerCase();
        message = message.replace("/close","").replace("/open","").replace(" ","");
        switch(message){
            case "global":
                getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).setGlobal(operation);
                event.getSubject().sendMessage("已设置全局消息的响应状态为"+operation);
                break;
            case "fish":
                getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).setFish(operation);
                event.getSubject().sendMessage("已设置钓鱼的响应状态为"+operation);
                break;
            case "casino":
                getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).setCasino(operation);
                event.getSubject().sendMessage("已设置娱乐游戏的响应状态为"+operation);
                break;
            case "responder":
                getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).setResponder(operation);
                event.getSubject().sendMessage("已设置关键词触发功能的响应状态为"+operation);
                break;
            case "game":
                getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).setGame(operation);
                event.getSubject().sendMessage("已设置所有游戏的响应状态为"+operation);
                break;
            default:
                event.getSubject().sendMessage("群设置指示词使用错误，请使用 /close 或者 /open 加上 空格 加上 global 或者 game 或者 casino 或者 responder 或者 fish 来开关相应内容。");
        }
    }

    public static void resetGroupConfig(GroupMessageEvent event){
        if(!event.getMessage().contentToString().equals("/default")) return;
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&(!IdentityUtil.isAdmin(event))) return;
        if(containsGroup(event.getGroup().getId())){
            getINSTANCE().groupConfigs.groupConfigList.remove(Objects.requireNonNull(getGroupIndex(event.getGroup().getId())).intValue());
        }
        addGroupConfig(event.getGroup().getId());
    }

    public static boolean globalConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        if(!getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isGlobal()){
            return IdentityUtil.isAdmin(event);
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

    public static boolean lotteryConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isLottery();
    }

    public static boolean gameConfig(GroupMessageEvent event){
        if(readGroupConfig(event.getGroup().getId())==null) addGroupConfig(event.getGroup().getId());
        return getINSTANCE().groupConfigs.groupConfigList.get(getGroupIndex(event.getGroup().getId())).isGame();
    }

    public static void addBlockedUser(GroupMessageEvent event){
        if(event.getSender().getPermission().equals(MemberPermission.MEMBER)&&!IdentityUtil.isAdmin(event)) return;
        if(!event.getMessage().contentToString().toLowerCase().contains("/blockmember")) return;
        String message = event.getMessage().contentToString().replace("/blockmember","");
        message = message.replace(" ","");
        if(!Pattern.compile("[0-9]*").matcher(message).matches()){
            event.getGroup().sendMessage("/blockmember 指示器使用错误，请添加QQ号");
            return;
        }
        long ID = 0;
        try {
            ID = Long.parseLong(message);
        } catch (Exception e){
            e.printStackTrace();
            event.getGroup().sendMessage("/blockmember 指示器使用错误，请添加QQ号");
            return;
        }
        if(ID==0){
            event.getGroup().sendMessage("/blockmember 指示器使用错误，请添加QQ号");
            return;
        }
        for(NormalMember nm:event.getGroup().getMembers()){
            if(nm.getId()==ID);
        }
    }

    public static void handle(GroupMessageEvent event){
        resetGroupConfig(event);
        changeGroupConfig(event);
    }

    public void ini(){}

}
