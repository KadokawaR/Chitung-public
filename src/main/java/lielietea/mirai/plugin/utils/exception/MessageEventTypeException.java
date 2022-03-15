package lielietea.mirai.plugin.utils.exception;

import net.mamoe.mirai.event.events.MessageEvent;

public class MessageEventTypeException extends RuntimeException {
    public MessageEventTypeException(MessageEvent event) {
        super("消息类型匹配错误！该消息事件实例为" + event.toString());
    }
}
