package mirai.chitung.plugin.utils;

import com.google.gson.Gson;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BotLoginUtil {


    static String AUTO_LOGIN_PATH = System.getProperty("user.dir") + File.separator +  "AutoLogin.json";

    BotLoginUtil(){}

    private static final BotLoginUtil INSTANCE;

    static {
        INSTANCE = new BotLoginUtil();
        initialize();
    }

    public LoginInfoClass loginInfos;

    static class LoginInfoClass{
        Map<Long,String> loginInfos;
        LoginInfoClass(){
            this.loginInfos = new HashMap<>();
            this.loginInfos.put(0L,"");
        }
    }

    public static BotLoginUtil getINSTANCE() {
        return INSTANCE;
    }

    static void initialize(){
        getINSTANCE().loginInfos = new LoginInfoClass();
        if(Touch.file(AUTO_LOGIN_PATH)){
            try {
                getINSTANCE().loginInfos = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(AUTO_LOGIN_PATH)), StandardCharsets.UTF_8))), LoginInfoClass.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Write.cover(new Gson().toJson(getINSTANCE().loginInfos),AUTO_LOGIN_PATH);
        }
    }

    static LoginInfoClass readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(AUTO_LOGIN_PATH)), StandardCharsets.UTF_8))), LoginInfoClass.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void botLogin(){
        if(getINSTANCE().loginInfos==null) return;
        for(long ID:getINSTANCE().loginInfos.loginInfos.keySet()) {
            if(ID==0) continue;
            try {
                Bot bot = BotFactory.INSTANCE.newBot(ID, getINSTANCE().loginInfos.loginInfos.get(ID), new BotConfiguration() {{
                    fileBasedDeviceInfo();
                }});
                if (!bot.isOnline()) {
                    bot.login();
                }
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

            if(!getINSTANCE().loginInfos.loginInfos.containsKey(ID)){
                event.getSubject().sendMessage("登陆列表内没有该QQ号。");
                return;
            }

            for(long id:getINSTANCE().loginInfos.loginInfos.keySet()) {
                if(id==ID) {
                    try {
                        Bot bot = BotFactory.INSTANCE.newBot(ID, getINSTANCE().loginInfos.loginInfos.get(ID));
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
