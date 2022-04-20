package mirai.chitung.plugin.administration.config;

import mirai.chitung.plugin.utils.IdentityUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.BotJoinGroupEvent;
import net.mamoe.mirai.event.events.BotLeaveEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigHandler {

    static String BASIC_CONFIGURATION_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator + "Config.json";

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
                getINSTANCE().config = new Gson().fromJson(new String(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))).getBytes(StandardCharsets.UTF_8)), Config.class);
                //getINSTANCE().config = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))), Config.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    static Config readRecord(){
        try {
            //return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))), Config.class);
            return new Gson().fromJson(new String(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BASIC_CONFIGURATION_PATH)))).getBytes(StandardCharsets.UTF_8)), Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void writeRecord(){
        String jsonString =  new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().config);
        Write.cover(jsonString, BASIC_CONFIGURATION_PATH,true);
    }

    static void getCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(!event.getMessage().contentToString().equalsIgnoreCase("/config")) return;
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append("addFriend: ").append(String.valueOf(getINSTANCE().config.getRc().isAddFriend())).append("\n");
        mcb.append("addGroup: ").append(String.valueOf(getINSTANCE().config.getRc().isAddGroup())).append("\n");
        mcb.append("answerFriend: ").append(String.valueOf(getINSTANCE().config.getRc().isAnswerFriend())).append("\n");
        mcb.append("answerGroup: ").append(String.valueOf(getINSTANCE().config.getRc().isAnswerGroup())).append("\n");
        mcb.append("autoAnswer: ").append(String.valueOf(getINSTANCE().config.getRc().isAutoAnswer())).append("\n");
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    static void changeCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().equalsIgnoreCase("/config -h")) {
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
        return getINSTANCE().config.getRc().isAddFriend();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canAddGroup(){
        return getINSTANCE().config.getRc().isAddGroup();
    }

    public static boolean canAnswerFriend(){
        return getINSTANCE().config.getRc().isAnswerFriend();
    }

    public static boolean canAnswerGroup(){
        return getINSTANCE().config.getRc().isAnswerGroup();
    }

    public static boolean canAutoAnswer(){
        return getINSTANCE().config.getRc().isAutoAnswer();
    }

    static void reset(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().toLowerCase().contains("/reset")&&(event.getMessage().contentToString().toLowerCase().contains("config"))){
            getINSTANCE().config=readRecord();
            event.getSubject().sendMessage("已经重置 Config 配置文件。");
        }

    }

    public static void react(MessageEvent event){
        getCurrentBotConfig(event);
        changeCurrentBotConfig(event);
        reset(event);
    }

    public static String getName(BotJoinGroupEvent event){
        if(getINSTANCE().config.getBotName().equals("")) return event.getBot().getNick();
        return getINSTANCE().config.getBotName();
    }

    public static String getName(Bot bot){
        if(getINSTANCE().config.getBotName().equals("")) return bot.getNick();
        return getINSTANCE().config.getBotName();
    }

    public static String getName(BotLeaveEvent event){
        if(getINSTANCE().config.getBotName().equals("")) return event.getBot().getNick();
        return getINSTANCE().config.getBotName();
    }


    public void ini(){
        System.out.println("Initialize Config Handler");
    }
}
