package lielietea.mirai.plugin.core.responder.help;

import lielietea.mirai.plugin.administration.config.ConfigHandler;
import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.utils.exception.NoHandlerMethodMatchException;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;


public class Help implements MessageResponder<MessageEvent> {
    static final List<MessageType> type = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static final Map<Predicate<MessageEvent>, String> MAP = new HashMap<>();

    static {
        {
            MAP.put(event -> event.getMessage().contentToString().equals("/help") || event.getMessage().contentToString().equals("帮助"), Speech.HELP);
            MAP.put(event -> event.getMessage().contentToString().equals("/conta") || event.getMessage().contentToString().equals("联系作者"), Speech.CONTA);
            MAP.put(event -> event.getMessage().contentToString().equals("/intro") || event.getMessage().contentToString().equals("介绍"), Speech.INTRO);
            MAP.put(event -> event.getMessage().contentToString().equals("/discl") || event.getMessage().contentToString().equals("/免责协议"), Speech.DISCL);
            MAP.put(event -> event.getMessage().contentToString().equals("/usage") || event.getMessage().contentToString().equals("用法"), Speech.USAGE);
        }
    }

    @Override
    public boolean match(MessageEvent event) {
        for (Predicate<MessageEvent> predicate : MAP.keySet()) {
            if (predicate.test(event)) return true;
        }
        return false;
    }

    @Override
    public RespondTask handle(MessageEvent event) throws NoHandlerMethodMatchException {
        for (Map.Entry<Predicate<MessageEvent>, String> entry : MAP.entrySet()) {
            if (entry.getKey().test(event))
                return RespondTask.of(event, entry.getValue(), this);
        }
        throw new NoHandlerMethodMatchException("帮助", event);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return type;
    }


    @Override
    public String getName() {
        return "帮助";
    }

    public static class Speech {
        public static final String HELP = "输入下方带有斜杠的关键词可以获得相关信息。\n\n" +
                "/intro "+ConfigHandler.getName(Bot.getInstances().get(0))+"简介\n" +
                "/usage 如何在自己的群中使用"+ConfigHandler.getName(Bot.getInstances().get(0))+"\n" +
                "/discl 免责协议\n" +
                "/funct 功能列表\n" +
                "/conta 联系运营者和开发者";
        public static final String INTRO = "本机器人使用七筒开放版——一个致力于服务简体中文 Furry 社群的 QQ 机器人项目，皆在试图为群聊增加一些乐趣。请发送/funct 来了解如何使用本机器人。注意，不要和我，也不要和生活太较真。";
        public static final String USAGE = "点击头像添加"+ConfigHandler.getName(Bot.getInstances().get(0)) +"为好友，并将其邀请到QQ群聊中，即可在该群聊中使用服务。" + "如果需要查看功能列表，请输入/funct。";
        public static final String DISCL = "本项目由七筒开放版驱动，但并非由官方直接运营，如有任何问题请联系该机器人的运营者。如需使用该项目请查询 Github - Chitung Public，或者联系七筒项目的开发者。";
        public static String CONTA = "如果需要联系"+ ConfigHandler.getName(Bot.getInstances().get(0)) +"的运营者，请直接添加"+ ConfigHandler.getName(Bot.getInstances().get(0)) +"好友，并在发送消息的开头注明”意见反馈“。只有含有“意见反馈”字样的单条消息才会被接收。如需要联系七筒的开发者，请添加号码：2955808839。";
    }
}
