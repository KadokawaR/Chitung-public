package mirai.chitung.plugin.core.responder.universalrespond;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mirai.chitung.plugin.core.harbor.Harbor;
import mirai.chitung.plugin.core.responder.universalrespond.respondenum.ListKind;
import mirai.chitung.plugin.core.responder.universalrespond.respondenum.MessageKind;
import mirai.chitung.plugin.utils.IdentityUtil;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class URManager {
    static final String UR_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator + "UniversalResponder.json";

    URManager(){}

    private static final URManager INSTANCE;

    static class URList{
        List<URData> universalRespondList;
        URList(){
            this.universalRespondList = new ArrayList<URData>(){{add(new URData());}};
        }
    }

    static {
        INSTANCE = new URManager();
        initialize();
    }

    public URList urList;

    public static URManager getINSTANCE() {
        return INSTANCE;
    }

    public static void initialize(){
        getINSTANCE().urList = new URList();
        if(Touch.file(UR_PATH)){
            try {
                getINSTANCE().urList = new Gson().fromJson(Read.fromFile(UR_PATH), URList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    public static URList readRecord(){
        URList urList = new URList();
        try {
            urList = new Gson().fromJson(Read.fromFile(UR_PATH), URList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        URList copiedURList = urList;
        for(URData ur:copiedURList.universalRespondList){
            if(ur.getMessageKind()==null||ur.getUserList()==null||ur.getTriggerKind()==null||ur.getAnswer()==null||ur.getPattern()==null){
                urList.universalRespondList.remove(ur);
            }

            if(ur.getPattern().isEmpty()||ur.getAnswer().isEmpty()){
                urList.universalRespondList.remove(ur);
            }

        }

        return urList;
    }

    public static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().urList);
        Write.cover(jsonString, UR_PATH);
    }

    static boolean contentMatch(String string, URData ur){
        switch(ur.getTriggerKind()){
            case Equal:
                for(String pattern:ur.getPattern()){
                    if(string.equals(pattern)) return true;
                }
                break;
            case Contain:
                for(String pattern:ur.getPattern()){
                    if(string.contains(pattern)) return true;
                }
                break;
        }
        return false;
    }

    static boolean messageResponseKindMatch(MessageEvent event, URData data){
        if(data.getMessageKind().equals(MessageKind.Any)) return true;
        if(event instanceof GroupMessageEvent){
            return data.getMessageKind().equals(MessageKind.Group);
        } else {
            return data.getMessageKind().equals(MessageKind.Friend);
        }
    }

    static boolean conditionMatch(MessageEvent event, URData data){

        for(URListData urListData:data.getUserList()) {
            if (event instanceof GroupMessageEvent) {

                switch (urListData.userKind) {
                    case Friend:
                        continue;
                    case Group:
                        if (urListData.listKind.equals(ListKind.White)) {
                            return urListData.IDList.contains(event.getSubject().getId());
                        } else {
                            return !urListData.IDList.contains(event.getSubject().getId());
                        }
                    case User:
                    case GroupMember:
                        if (urListData.listKind.equals(ListKind.White)) {
                            return urListData.IDList.contains(event.getSender().getId());
                        } else {
                            return !urListData.IDList.contains(event.getSender().getId());
                        }

                }

            } else {

                switch (urListData.userKind) {
                    case Group:
                    case GroupMember:
                        continue;
                    case User:
                    case Friend:
                        if (urListData.listKind.equals(ListKind.White)) {
                            return urListData.IDList.contains(event.getSender().getId());
                        } else {
                            return !urListData.IDList.contains(event.getSender().getId());
                        }
                }
            }
        }

        return true;
    }

    static void respond(MessageEvent event,String message){
        for(URData ur:getINSTANCE().urList.universalRespondList) {

            if(!contentMatch(message,ur)) continue;
            if(!messageResponseKindMatch(event,ur)) continue;
            if(!conditionMatch(event,ur)) continue;

            Random random = new Random();
            int n = random.nextInt(ur.getAnswer().size());
            event.getSubject().sendMessage(ur.getAnswer().get(n));

            Harbor.count(event);
            return;
        }
    }

    static void reset(MessageEvent event,String message){
        if(!IdentityUtil.isAdmin(event)) return;
        if(message.toLowerCase().contains("/reset")&&(message.toLowerCase().contains("ur"))){
            getINSTANCE().urList=readRecord();
            event.getSubject().sendMessage("已经重置 Universal Responder 的配置文件。");
        }
    }

    public static void check(MessageEvent event,String message){
        if(!IdentityUtil.isAdmin(event)) return;

        if(message.equalsIgnoreCase("/check ur")||message.equalsIgnoreCase("/check -ur")||message.equalsIgnoreCase("查看通用响应")){

            MessageChainBuilder mcb = new MessageChainBuilder().append("通用响应关键词：\n");

            for(int i=0;i<getINSTANCE().urList.universalRespondList.size();i++){

                URData ur = getINSTANCE().urList.universalRespondList.get(i);
                mcb.append("关键词：");
                StringBuilder sb = new StringBuilder();

                for(String s:ur.getPattern()){

                    sb.append(s);
                    sb.append(" ");

                }

                mcb.append(sb.toString().trim()).append("\n").append("响应模式：").append(String.valueOf(ur.getTriggerKind())).append("\n");
                mcb.append("当前环境响应状态：").append(String.valueOf(conditionMatch(event,ur)&&messageResponseKindMatch(event,ur)));

                if(i!=getINSTANCE().urList.universalRespondList.size()-1){
                    mcb.append("\n\n");
                }

            }

            event.getSubject().sendMessage(mcb.asMessageChain());
        }
    }

    public static void handle(MessageEvent event){
        String message = event.getMessage().contentToString();
        reset(event,message);
        respond(event,message);
        check(event,message);
    }

    public void ini(){
        System.out.println("Initialize Universal Responder Manager");
    }
}
