package chitung.plugin.utils;

import net.mamoe.mirai.Bot;

import java.util.List;
import java.util.Objects;

public class MessageUtil {

    /**
     * 指定某个Bot，向开发群发送消息通知
     */
    public static void notifyDevGroup(String content, long botId){
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            if(bot.getId() == botId){
                for(long ID:IdentityUtil.getDevGroup()){
                    if(bot.getGroup(ID)!=null) Objects.requireNonNull(bot.getGroup(ID)).sendMessage(content);
                }
            }
        }
    }

    /**
     * 指定某个Bot，向开发群发送消息通知
     */
    public static void notifyDevGroup(String content, Bot bot){
        for(long ID:IdentityUtil.getDevGroup()){
            if(bot.getGroup(ID)!=null) Objects.requireNonNull(bot.getGroup(ID)).sendMessage(content);
        }
    }

}
