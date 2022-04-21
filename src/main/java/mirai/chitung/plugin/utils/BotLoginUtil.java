package mirai.chitung.plugin.utils;

import com.google.gson.Gson;
import mirai.chitung.plugin.core.responder.Blacklist;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.events.MessageEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotLoginUtil {


    static String BLACKLIST_PATH = System.getProperty("user.dir") +  "AutoLogin.json";

    BotLoginUtil(){}

    private static final BotLoginUtil INSTANCE;

    static {
        INSTANCE = new BotLoginUtil();
        initialize();
    }

    public Map<Long,String> loginInfos;

    static class loginInfoClass{
        Map<Long,String> loginInfos;
        loginInfoClass(){
            this.loginInfos = new HashMap<>();
        }
    }

    public static BotLoginUtil getINSTANCE() {
        return INSTANCE;
    }

    static void initialize(){
        getINSTANCE().loginInfos = new loginInfoClass().loginInfos;
        if(Touch.file(BLACKLIST_PATH)){
            try {
                getINSTANCE().loginInfos = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(BLACKLIST_PATH)), StandardCharsets.UTF_8))), loginInfoClass.class).loginInfos;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static Map<Long,String> readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(BLACKLIST_PATH)), StandardCharsets.UTF_8))), loginInfoClass.class).loginInfos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void botLogin(){
        for(long ID:getINSTANCE().loginInfos.keySet()) {
            try {
                Bot bot = BotFactory.INSTANCE.newBot(ID, getINSTANCE().loginInfos.get(ID));
                if (!bot.isOnline()) bot.login();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void react(MessageEvent event){
        if(!IdentityUtil.isAdmin(event)) return;
        String message = event.getMessage().contentToString();

        if(message.equalsIgnoreCase("/reset -bl")){
            getINSTANCE().loginInfos=readRecord();
            return;
        }
        
        if(message.equalsIgnoreCase("/reboot")){
            for(Bot bot:Bot.getInstances()){
                try{
                    bot.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            botLogin();
        }

        if(message.toLowerCase().contains("/reboot ")){
            String rawString = message.toLowerCase().replace("/reboot","").replace(" ","");
            long ID = Long.parseLong(rawString);

            if(ID<=10000){
                event.getSubject().sendMessage("QQ号格式错误。");
                return;
            }

            if(!getINSTANCE().loginInfos.containsKey(ID)){
                event.getSubject().sendMessage("登陆列表内没有该QQ号。");
                return;
            }

            for(long id:getINSTANCE().loginInfos.keySet()) {
                if(id==ID) {
                    try {
                        Bot bot = BotFactory.INSTANCE.newBot(ID, getINSTANCE().loginInfos.get(ID));
                        if (!bot.isOnline()){
                            bot.login();
                            event.getSubject().sendMessage(id+"已经成功登录。");
                        } else {
                            event.getSubject().sendMessage(id+"已经在线，无法重复登陆。");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
