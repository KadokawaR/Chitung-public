package lielietea.mirai.plugin.utils.exception;

import net.mamoe.mirai.event.events.MessageEvent;

public class NoHandlerMethodMatchException extends RuntimeException {
    public NoHandlerMethodMatchException(String inFunction, MessageEvent event) {
        super("在" + inFunction + "中尝试匹配处理方法失败，对应消息为“" + event.getMessage().contentToString() + "”");
    }
}
