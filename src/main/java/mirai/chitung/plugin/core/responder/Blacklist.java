package mirai.chitung.plugin.core.responder;

import mirai.chitung.plugin.utils.IdentityUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Blacklist {

    static String BLACKLIST_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator + "Blacklist.json";

    Blacklist(){}

    private static final Blacklist INSTANCE;

    static class BlackListClass{
        List<Long> friendBlacklist;
        List<Long> groupBlacklist;
        BlackListClass(){
            this.friendBlacklist = new ArrayList<>();
            this.groupBlacklist = new ArrayList<>();
        }
    }

    static {
        INSTANCE = new Blacklist();
        initialize();
    }

    public BlackListClass blackListClass;

    public static Blacklist getINSTANCE() {
        return INSTANCE;
    }

    static void initialize(){
        getINSTANCE().blackListClass = new BlackListClass();
        if(Touch.file(BLACKLIST_PATH)){
            try {
                getINSTANCE().blackListClass = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(BLACKLIST_PATH)), StandardCharsets.UTF_8))), BlackListClass.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    static BlackListClass readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(BLACKLIST_PATH)), StandardCharsets.UTF_8))), BlackListClass.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().blackListClass);
        Write.cover(jsonString, BLACKLIST_PATH);
    }

    public enum BlockKind{
        Friend,
        Group
    }

    static void addBlock(long ID,BlockKind bk){
        switch(bk){
            case Friend:
                getINSTANCE().blackListClass.friendBlacklist.add(ID);
                writeRecord();
                getINSTANCE().blackListClass=readRecord();
                break;
            case Group:
                getINSTANCE().blackListClass.groupBlacklist.add(ID);
                writeRecord();
                getINSTANCE().blackListClass=readRecord();
        }
    }

    static void deleteBlock(long ID,BlockKind bk){
        switch(bk){
            case Friend:
                getINSTANCE().blackListClass.friendBlacklist.remove(ID);
                writeRecord();
                getINSTANCE().blackListClass=readRecord();
                break;
            case Group:
                getINSTANCE().blackListClass.groupBlacklist.remove(ID);
                writeRecord();
                getINSTANCE().blackListClass=readRecord();
        }
    }

    //太奇怪了这个操作
    static void blockGroupInGroup(GroupMessageEvent event,String message){
        if(!IdentityUtil.isAdmin(event)) return;
        if(message.equalsIgnoreCase("/block")) addBlock(event.getGroup().getId(),BlockKind.Group);
    }

    static void block(MessageEvent event,String message){
        if(!IdentityUtil.isAdmin(event)) return;
        if(!message.toLowerCase().contains("/block ")||!message.toLowerCase().contains("/block-")) return;
        String rawString = message.toLowerCase().replace("/block","").replace(" ","").replace("-","");
        String strID = Pattern.compile("[^0-9]").matcher(rawString).replaceAll(" ").trim();

        Long ID=null;
        BlockKind bk=null;

        if(rawString.contains("g")){
            bk = BlockKind.Group;
        }

        if(rawString.contains("f")){
            bk = BlockKind.Friend;
        }

        if(bk==null||(rawString.contains("g")&&(rawString.contains("f")))){
            event.getSubject().sendMessage("命令格式错误，请重新输入。");
            return;
        }

        try{
            ID = Long.parseLong(strID);
        } catch(Exception e){
            e.printStackTrace();
        }

        if(ID==null||ID<10000){
            event.getSubject().sendMessage("账号格式错误，请重新输入。");
            return;
        }

        if(bk.equals(BlockKind.Friend)&&IdentityUtil.isAdmin(ID)){
            event.getSubject().sendMessage("你怎么会想到屏蔽管理员？");
            return;
        }

        addBlock(ID,bk);

        switch(bk){
            case Friend:
                event.getSubject().sendMessage("已屏蔽用户"+ID);
            case Group:
                event.getSubject().sendMessage("已屏蔽群聊"+ID);
        }

    }

    static void unblock(MessageEvent event,String message){
        if(!IdentityUtil.isAdmin(event)) return;
        if(!message.toLowerCase().contains("/unblock ")||!message.toLowerCase().contains("/unblock-")) return;
        String rawString = message.toLowerCase().replace("/unblock","").replace(" ","").replace("-","");
        String strID = Pattern.compile("[^0-9]").matcher(rawString).replaceAll(" ").trim();

        Long ID=null;
        BlockKind bk=null;

        if(rawString.contains("g")){
            bk = BlockKind.Group;
        }

        if(rawString.contains("f")){
            bk = BlockKind.Friend;
        }

        if(bk==null||(rawString.contains("g")&&(rawString.contains("f")))){
            event.getSubject().sendMessage("命令格式错误，请重新输入。");
            return;
        }

        try{
            ID = Long.parseLong(strID);
        } catch(Exception e){
            e.printStackTrace();
        }

        if(ID==null||ID<10000){
            event.getSubject().sendMessage("账号格式错误，请重新输入。");
            return;
        }

        if(!isBlocked(ID,bk)){
            switch(bk){
                case Friend:
                    event.getSubject().sendMessage("用户"+ID+"未被屏蔽。");
                case Group:
                    event.getSubject().sendMessage("群聊"+ID+"未被屏蔽。");
            }
            return;
        }

        deleteBlock(ID,bk);

        switch(bk){
            case Friend:
                event.getSubject().sendMessage("已解除屏蔽用户"+ID);
            case Group:
                event.getSubject().sendMessage("已解除屏蔽群聊"+ID);
        }
    }

    public static boolean isBlocked(long ID,BlockKind bk){

        if(IdentityUtil.isAdmin(ID)) return false;

        switch(bk){
            case Group:
                return getINSTANCE().blackListClass.groupBlacklist.contains(ID);
            case Friend:
                return getINSTANCE().blackListClass.friendBlacklist.contains(ID);
        }
        return false;

    }

    public static void operation(MessageEvent event){
        String message = event.getMessage().contentToString();
        if(event instanceof GroupMessageEvent){
            blockGroupInGroup((GroupMessageEvent) event,message);
        }
        block(event,message);
        unblock(event,message);
    }

    public void ini(){
        System.out.println("Initialize Blacklist");
    }
}
