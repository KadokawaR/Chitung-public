package lielietea.mirai.plugin.core.responder.help;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.utils.exception.NoHandlerMethodMatchException;
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
            //MAP.put(event -> event.getMessage().contentToString().equals("/funct") || event.getMessage().contentToString().equals("/功能"), Speech.FUNCT);
            MAP.put(event -> event.getMessage().contentToString().equals("/conta") || event.getMessage().contentToString().equals("联系作者"), Speech.CONTA);
            MAP.put(event -> event.getMessage().contentToString().equals("/intro") || event.getMessage().contentToString().equals("介绍"), Speech.INTRO);
            //MAP.put(event -> event.getMessage().contentToString().equals("/discl") || event.getMessage().contentToString().equals("/免责协议"), Speech.DISCLAIMER);
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

    static class Speech {
        static final String DISCLAIMER = "本项目仅限学习使用，不涉及到任何商业或者金钱用途，禁止用于非法行为。您的使用行为将被视为对本声明全部内容的认可。本声明在您邀请该账号（QQ账号：340865180）进入任何腾讯QQ群聊时生效。\n" +
                "\n" +
                "本项目在运作时，不可避免地会使用到您的QQ号、QQ昵称、群号、群昵称等信息。后台不会收集具体的聊天内容，如果您对此有所疑问，请停止使用本项目。基于维持互联网秩序的考量，请勿恶意使用本项目。本项目有权停止对任何对象的服务，任何解释权均归本项目开发组所有。\n" +
                "\n" +
                "本项目涉及或使用到的开源项目有：基于 AGPLv3 协议的 Mirai (https://github.com/mamoe/mirai) ，基于 Apache License 2.0 协议的谷歌 Gson (https://github.com/google/gson) ，清华大学开放中文词库 (http://thuocl.thunlp.org/) ，动物图片来自互联网开源动物图片API Shibe.online(shibes as a service)、Dog.ceo (The internet's biggest collection of open source dog pictures.)、random.dog (Hello World, This Is Dog)。\n";
        static final String HELP = "输入下方带有斜杠的关键词可以获得相关信息。\n\n" +
                "/intro 七筒简介\n" +
                "/usage 如何在自己的群中使用七筒\n" +
                "/discl 免责协议\n" +
                "/funct 功能列表\n" +
                "/conta 联系开发者";
        static final String INTRO = "七筒是一个用于服务简体中文 Furry 社群的 QQ 机器人项目，皆在试图为群聊增加一些乐趣。请发送/funct 来了解如何使用七筒。注意，不要和我，也不要和生活太较真。";
        static final String USAGE = "点击头像添加七筒为好友（或者添加号码 340865180），并将其邀请到QQ群聊中，即可在该群聊中使用七筒的服务。\n\n" +
                "如果需要查看七筒的功能列表，请输入/funct。";
        static final String FUNCT = "输入下方的关键词即可使用对应功能。\n\n" +
                "兽设 -> 获取你的今日兽设。\n" +
                "猜麻将 -> 随机生成五张麻将牌，第一个猜中全部的人是赢家。\n" +
                "求签 -> 查看你的今日运势。\n" +
                "/dice8 或者 .3d10（数字可变）-> 掷骰子。\n" +
                "/jetpack -> 使用喷气背包将七筒移动到任意地点。\n" +
                "\n" +
                "奶茶 or 喝什么 -> 每个小时召唤一杯奶茶，不过分吧？\n" +
                "Ok Pizza 或者 /pizza -> 想来个披萨吗？\n" +
                "Ok Meal 或者 XX吃什么（比如午饭）或者 /meal -> 来点吃饭的好建议。\n" +
                "\n" +
                "Ok Winner 或者 /winner -> 获取本群今日的幸运用户！\n" +
                "Ok Bummer 或者 /bummer -> [需要管理员权限] 想来点刺激的吗？来，试试看！\n" +
                "Ok C4 或者 /c4 -> [需要管理员权限] 每日仅能触发一次的究极刺激挑战！\n" +
                "\n" +
                "Ok Dog 或者 /dog -> 来点狗狗！\n" +
                "Ok Husky 或者 /husky -> 来点二哈！\n" +
                "Ok Shiba 或者 /shiba-> 来点柴柴！\n" +
                "Ok 伯恩山 或者 /bernese -> 来点伯恩山！\n" +
                "Ok 德牧 或者 /gsd -> 来点德牧！\n" +
                "Ok 阿拉 或者 /malamute -> 来点阿拉斯加！\n" +
                "Ok 萨摩耶 或者 /samoyed -> 来点萨摩耶\n" +
                "Ok Cat 或者 /cat -> 来点猫猫！\n" +
                "\n" +
                "请注意，不同功能的更新频率不同。求签与兽设每日更新一次，奶茶每小时更新一次，群组winner每日更新一次。";
        static final String CONTA = "如果需要联系七筒的开发者，请直接添加七筒好友，并在发送消息的开头注明”意见反馈“。只有含有“意见反馈”字样的单条消息才会被接收。如有其他问题，请添加开发者好友，号码：2955808839。";
    }
}
