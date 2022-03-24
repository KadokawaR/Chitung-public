package lielietea.mirai.plugin.administration.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lielietea.mirai.plugin.utils.IdentityUtil;
import lielietea.mirai.plugin.utils.fileutils.Read;
import lielietea.mirai.plugin.utils.fileutils.Touch;
import lielietea.mirai.plugin.utils.fileutils.Write;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.*;

public class ConfigHandler {

    static String BASIC_CONFIGURATION_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Config.json";

    ConfigHandler(){}

    private static final ConfigHandler INSTANCE;

    static {
        INSTANCE = new ConfigHandler();
        initialize();
    }

    public Config config;

    public static ConfigHandler getINSTANCE() {
        return INSTANCE;
    }

    static void initialize(){
        getINSTANCE().config = new Config();
        if(Touch.file(BASIC_CONFIGURATION_PATH)){
            try {
                getINSTANCE().config = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))), Config.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    static Config readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))), Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().config);
        Write.cover(jsonString, BASIC_CONFIGURATION_PATH);
    }

    static void getCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(!event.getMessage().contentToString().equals("/config")) return;
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append("addFriend: ").append(String.valueOf(getINSTANCE().config.rc.addFriend)).append("\n");
        mcb.append("addGroup: ").append(String.valueOf(getINSTANCE().config.rc.addGroup)).append("\n");
        mcb.append("answerFriend: ").append(String.valueOf(getINSTANCE().config.rc.answerFriend)).append("\n");
        mcb.append("answerGroup: ").append(String.valueOf(getINSTANCE().config.rc.answerGroup)).append("\n");
        mcb.append("autoAnswer: ").append(String.valueOf(getINSTANCE().config.rc.autoAnswer)).append("\n");
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    static void changeCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().equals("/changeconfig")) {
            event.getSubject().sendMessage("使用/config+空格+数字序号+空格+true/false来开关配置。\n\n1:addFriend\n2:addGroup\n3:answerFriend\n4:answerGroup\n5:autoAnswer");
        }
        if(event.getMessage().contentToString().contains("/config")&&(event.getMessage().contentToString().contains("true")||event.getMessage().contentToString().contains("false"))){
            String[] messageSplit = event.getMessage().contentToString().split(" ");
            if(messageSplit.length!=3){
                event.getSubject().sendMessage("/config指示器使用错误。");
                return;
            }
            if(Boolean.parseBoolean(messageSplit[2]) &&!messageSplit[2].contains("r")){
                event.getSubject().sendMessage("Boolean设置错误。");
                return;
            }
            if(!Boolean.parseBoolean(messageSplit[2]) &&!messageSplit[2].contains("a")){
                event.getSubject().sendMessage("Boolean设置错误。");
                return;
            }

            getINSTANCE().config = readRecord();
            assert getINSTANCE().config != null;
            switch(messageSplit[1]){
                case "1":{
                    getINSTANCE().config.setAddFriend(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置acceptFriend为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "2":{
                    getINSTANCE().config.setAddGroup(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置acceptGroup为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "3":{
                    getINSTANCE().config.setAnswerFriend(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置answerFriend为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "4":{
                    getINSTANCE().config.setAnswerGroup(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置answerGroup为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "5":{
                    getINSTANCE().config.setAutoAnswer(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置sendNotice为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
            }
            writeRecord();
        }
    }

    public static boolean canAddFriend(){
        return getINSTANCE().config.rc.addFriend;
    }

    public static boolean canAddGroup(){
        return getINSTANCE().config.rc.addGroup;
    }
    public static boolean canAnswerFriend(){
        return getINSTANCE().config.rc.answerFriend;
    }

    public static boolean canAnswerGroup(){
        return getINSTANCE().config.rc.answerGroup;
    }

    public static boolean canAutoAnswer(){
        return getINSTANCE().config.rc.autoAnswer;
    }

    static void reset(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().contains("/reset")&&(event.getMessage().contentToString().contains("config")||event.getMessage().contentToString().contains("Config"))) getINSTANCE().config=readRecord();
        event.getSubject().sendMessage("已经重置 Universal Responder 配置文件。");
    }

    public static void react(MessageEvent event){
        getCurrentBotConfig(event);
        changeCurrentBotConfig(event);
        reset(event);
    }
}