package lielietea.mirai.plugin.utils;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.List;

public class MessageUtil {
    /**
     * 向开发群发送消息通知
     */
    public static void notifyDevGroup(String content){
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
            if (group != null) group.sendMessage(content);
        }
    }

    public static void notifyDevGroup(MessageChain messageChain){
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
            if (group != null) group.sendMessage(messageChain);
        }
    }

    /**
     * 指定某个Bot，向开发群发送消息通知
     */
    public static void notifyDevGroup(String content, long botId){
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            if(bot.getId() == botId){
                Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
                if (group != null) group.sendMessage(content);
            }
        }
    }

    /**
     * 指定某个Bot，向开发群发送消息通知
     */
    public static void notifyDevGroup(String content, Bot bot){
        Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
        if (group != null) group.sendMessage(content);
    }

    public static void notifyDevGroup(MessageChain messageChain, long botID){
        List<Bot> bots = Bot.getInstances();
        for (Bot bot : bots) {
            if(bot.getId() == botID){
                Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
                if (group != null) group.sendMessage(messageChain);
            }
        }
    }

    public static void notifyDevGroup(MessageChain messageChain, Bot bot){
        Group group = bot.getGroup(IdentityUtil.DevGroup.DEFAULT.getID());
        if (group != null) group.sendMessage(messageChain);
    }

}
