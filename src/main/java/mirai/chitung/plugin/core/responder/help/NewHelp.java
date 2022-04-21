package mirai.chitung.plugin.core.responder.help;

import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.core.harbor.Harbor;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.*;

public class NewHelp {

    enum HelpFunction{
        Help(Speech.HELP),
        Disclaimer(Speech.DISCL),
        Contact(Speech.CONTA),
        Usage(Speech.USAGE),
        Introduction(Speech.INTRO);

        private final String stringValue;

        HelpFunction(String value){
            this.stringValue=value;
        }

        public String getString(){
            return this.stringValue;
        }

    }

    static Map<HelpFunction,List<String>> helpFunctionListMap = new HashMap<>();
    static {
        helpFunctionListMap.put(HelpFunction.Help, new ArrayList<String>(){{add("/help");add("帮助");}});
        helpFunctionListMap.put(HelpFunction.Disclaimer, new ArrayList<String>(){{add("/discl");add("免责协议");add("/disclaimer");}});
        helpFunctionListMap.put(HelpFunction.Introduction, new ArrayList<String>(){{add("/intro");add("介绍");add("/introduction");}});
        helpFunctionListMap.put(HelpFunction.Usage, new ArrayList<String>(){{add("/usage");add("用法");}});
        helpFunctionListMap.put(HelpFunction.Contact, new ArrayList<String>(){{add("/conta");add("/contact");add("联系作者");}});
    }

    public static void handle(MessageEvent event){
        for(HelpFunction hf:helpFunctionListMap.keySet()){
            for(String keyWord:helpFunctionListMap.get(hf)){
                if(event.getMessage().contentToString().equals(keyWord)){
                    event.getSubject().sendMessage(hf.getString());
                    Harbor.count(event);
                    return;
                }
            }
        }
    }

    public static class Speech {
        public static String HELP = "输入下方带有斜杠的关键词可以获得相关信息。\n\n" +
                "/intro "+ ConfigHandler.getName(Bot.getInstances().get(0))+"简介\n" +
                "/usage 如何在自己的群中使用"+ConfigHandler.getName(Bot.getInstances().get(0))+"\n" +
                "/discl 免责协议\n" +
                "/conta 联系运营者和开发者";
        public static String INTRO = "本机器人使用七筒开放版——一个致力于服务简体中文 Furry 社群的 QQ 机器人项目，皆在试图为群聊增加一些乐趣。请发送/funct 来了解如何使用本机器人。注意，不要和我，也不要和生活太较真。";
        public static String USAGE = "点击头像添加"+ConfigHandler.getName(Bot.getInstances().get(0)) +"为好友，并将其邀请到QQ群聊中，即可在该群聊中使用服务。" + "如果需要查看功能列表，请输入/funct。";
        public static String DISCL = "本项目由七筒开放版驱动，但并非由官方直接运营，如有任何问题请联系该机器人的运营者。如需使用该项目请查询 Github - Chitung Public，或者联系七筒项目的开发者。";
        public static String CONTA = "如果需要联系"+ ConfigHandler.getName(Bot.getInstances().get(0)) +"的运营者，请直接添加"+ ConfigHandler.getName(Bot.getInstances().get(0)) +"好友，并在发送消息的开头注明”意见反馈“。只有含有“意见反馈”字样的单条消息才会被接收。如需要联系七筒的开发者，请添加号码：2955808839。";
    }
}
