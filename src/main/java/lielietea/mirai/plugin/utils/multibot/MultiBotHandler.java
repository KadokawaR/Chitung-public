package lielietea.mirai.plugin.utils.multibot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lielietea.mirai.plugin.utils.IdentityUtil;
import lielietea.mirai.plugin.utils.fileutils.Read;
import lielietea.mirai.plugin.utils.fileutils.Touch;
import lielietea.mirai.plugin.utils.fileutils.Write;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.*;

public class MultiBotHandler {

    final static String BOT_CONFIGURATION_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "botconfig.json";
    public enum BotName{
        //Chitung(340865180L),     //七筒#1
        Chitung1(3628496803L),   //七筒#2
        Chitung2(2429465624L ),  //七筒#3
        Chitung3(3582637350L);    //七筒#4

        private final long value;

        private BotName(long ID) { this.value = ID; }

        public long getValue() { return this.value; }

        public static BotName get(long ID){
            for(BotName bn:BotName.values()){
                if(bn.getValue()==ID) return bn;
            }
            return null;
        }
    }

    public static BotName getBotName(long ID){
        for(BotName bt : BotName.values()){
            if (bt.getValue()==ID) return bt;
        }
        return null;
    }

    MultiBotHandler(){}

    private static final MultiBotHandler INSTANCE;

    static {
        INSTANCE = new MultiBotHandler();
        initialize();
    }

    public BotConfigList botConfigList;

    public static MultiBotHandler getINSTANCE() {
        return INSTANCE;
    }

    public static void initialize(){
        getINSTANCE().botConfigList = new BotConfigList();
        if(Touch.file(BOT_CONFIGURATION_PATH)){
            try {
                getINSTANCE().botConfigList = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BOT_CONFIGURATION_PATH)))), BotConfigList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (BotName bt : BotName.values()) {
                getINSTANCE().botConfigList.botConfigs.add(new BotConfig(bt.getValue()));
            }
            writeRecord();
        }
    }

    public static BotConfigList readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(BOT_CONFIGURATION_PATH)))), BotConfigList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().botConfigList);
        Write.cover(jsonString, BOT_CONFIGURATION_PATH);
    }

    private Integer getIndexOfBot(long ID){
        int index=0;
        for(BotConfig bc:getINSTANCE().botConfigList.botConfigs){
            if(bc.ID==ID) return index;
            index+=1;
        }
        return null;
    }

    public static String rejectInformation(long ID){
        StringBuilder sb = new StringBuilder();
        sb.append("该账号已经停止接受加入新群组，请尝试如下账号：");
        int count = 0;
        for(Bot bot:Bot.getInstances()){
            if(bot.getId()==ID) continue;
            if(getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(bot.getId())).acceptGroup){
                sb.append("\n").append(bot.getId()).append(" ").append(bot.getNick());
                count++;
            }
        }
        if(count==0) sb.append("\n\n艹 我们没号了，先别加群了。");
        return sb.toString();
    }

    public static boolean canAcceptGroup(long ID){
        return getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(ID)).acceptGroup;
    }

    public static boolean canAcceptFriend(long ID){
        return getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(ID)).acceptFriend;
    }

    public static boolean canAnswerGroup(GroupMessageEvent event){
        if(IdentityUtil.isAdmin(event)) return true;
        return getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).answerGroup;
    }

    public static boolean canAnswerFriend(FriendMessageEvent event){
        if(IdentityUtil.isAdmin(event)) return true;
        return getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).answerFriend;
    }

    public static boolean canSendNotice(Bot bot){
        return getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(bot.getId())).sendNotice;
    }

    public static void getCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(!event.getMessage().contentToString().equals("/config")) return;
        BotConfig bc = getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId()));
        MessageChainBuilder mcb = new MessageChainBuilder();
        mcb.append("acceptFriend: ").append(String.valueOf(bc.acceptFriend)).append("\n");
        mcb.append("acceptGroup: ").append(String.valueOf(bc.acceptGroup)).append("\n");
        mcb.append("answerFriend: ").append(String.valueOf(bc.answerFriend)).append("\n");
        mcb.append("answerGroup: ").append(String.valueOf(bc.answerGroup)).append("\n");
        mcb.append("sendNotice: ").append(String.valueOf(bc.sendNotice)).append("\n");
        event.getSubject().sendMessage(mcb.asMessageChain());
    }

    public static void changeCurrentBotConfig(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        if(event.getMessage().contentToString().equals("/changeconfig")) {
            event.getSubject().sendMessage("使用/config+空格+数字序号+空格+true/false来开关配置。\n\n1:acceptFriend\n2:acceptGroup\n3:answerFriend\n4:answerGroup\n5:sendNotice");
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

            getINSTANCE().botConfigList = readRecord();
            switch(messageSplit[1]){
                case "1":{
                    getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).setAcceptFriend(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置acceptFriend为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "2":{
                    getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).setAcceptGroup(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置acceptGroup为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "3":{
                    getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).setAnswerFriend(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置answerFriend为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "4":{
                    getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).setAnswerGroup(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置answerGroup为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
                case "5":{
                    getINSTANCE().botConfigList.botConfigs.get(getINSTANCE().getIndexOfBot(event.getBot().getId())).setSendNotice(Boolean.parseBoolean(messageSplit[2]));
                    event.getSubject().sendMessage("已设置sendNotice为"+Boolean.parseBoolean(messageSplit[2]));
                    break;
                }
            }
            writeRecord();
        }
    }

    public static void react(MessageEvent event){
        getCurrentBotConfig(event);
        changeCurrentBotConfig(event);
    }
}
