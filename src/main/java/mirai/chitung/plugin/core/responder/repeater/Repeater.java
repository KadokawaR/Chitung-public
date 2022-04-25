package mirai.chitung.plugin.core.responder.repeater;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Repeater {

    static Map<Long, MessageHolder> messageContainer = new ConcurrentHashMap<>();

    public static void handle(GroupMessageEvent event) {
        if (isValidMessage(event.getMessage())) {
            MessageChain messages = MessageUtils.newChain(event.getMessage().stream().filter(singleMessage -> !(singleMessage instanceof MessageSource)).collect(Collectors.toList()));
            long id = event.getGroup().getId();
            if (!messageContainer.containsKey(id)) {
                messageContainer.put(id, new MessageHolder(messages, 1));
            } else {
                if (messages.equals(messageContainer.get(id).messages)) {
                    messageContainer.get(id).count++;
                    if (messageContainer.get(id).count == 3) {
                        event.getGroup().sendMessage(messageContainer.get(id).messages);
                    }
                } else {
                    messageContainer.remove(event.getGroup().getId());
                }
            }
        } else {
            messageContainer.remove(event.getGroup().getId());
        }
    }

    // 一旦机器人说话就清空记录
    public static void flush(Group group) {
        messageContainer.remove(group.getId());
    }

    // 判断消息是否仅由文字，图片，At构成，且不包含引用回复
    private static boolean isValidMessage(MessageChain chain) {
        if (chain.contains(QuoteReply.Key)) {
            return false;
        }
        for (Message message : chain) {
            if (!(message instanceof MessageSource ||message instanceof PlainText || message instanceof At || message instanceof Image))
                return false;
        }
        return true;
    }


    static class MessageHolder {
        MessageChain messages;
        int count;

        public MessageHolder(MessageChain messages, int count) {
            this.messages = messages;
            this.count = count;
        }
    }
}