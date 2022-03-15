package lielietea.mirai.plugin.core.responder;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RespondTask {
    final Contact source;
    final Contact sender;
    final List<Object> action = new ArrayList<>();
    final String responderName;
    final UUID responderUUID;
    Contact target;
    String note;

    RespondTask(MessageEvent source, MessageChain message, MessageResponder responder) {
        this.source = source.getSubject();
        sender = source.getSender();
        this.target = source.getSubject();
        action.add(message);
        responderName = responder.getName();
        responderUUID = responder.getUUID();
    }

    RespondTask(MessageEvent source, MessageResponder responder) {
        this.source = source.getSubject();
        sender = source.getSender();
        this.target = source.getSubject();
        responderName = responder.getName();
        responderUUID = responder.getUUID();
    }

    public static RespondTask of(MessageEvent source, String message, MessageResponder responder) {
        return new RespondTask(source, new MessageChainBuilder().append(message).build(), responder);
    }

    public static RespondTask of(MessageEvent source, MessageChain message, MessageResponder responder) {
        return new RespondTask(source, message, responder);
    }

    public void execute() {
        for (Object obj : action) {
            if (obj instanceof MessageChain) {
                target.sendMessage((MessageChain) obj);
            } else if (obj instanceof Runnable) {
                ((Runnable) obj).run();
            }
        }
    }

    public Contact getSender() {
        return sender;
    }

    public String getResponderName() {
        return responderName;
    }

    public UUID getResponderUUID() {
        return responderUUID;
    }

    public String getNote() {
        return note;
    }

    public Contact getSource() {
        return source;
    }

    public static class Builder {
        final RespondTask onBuild;

        public Builder(MessageEvent event, MessageResponder responder) {
            onBuild = new RespondTask(event, responder);
        }

        public Builder changeTarget(Contact target) {
            onBuild.target = target;
            return this;
        }

        public Builder addMessage(String message) {
            onBuild.action.add(new MessageChainBuilder().append(message).build());
            return this;
        }

        public Builder addMessage(MessageChain message) {
            onBuild.action.add(message);
            return this;
        }

        public Builder addTask(Runnable task) {
            onBuild.action.add(task);
            return this;
        }

        /**
         * 为包添加备注信息，一般是报错或者给管理员看的提示
         */
        public Builder addNote(String note) {
            if (onBuild.note == null)
                onBuild.note = note;
            else {
                StringBuilder builder = new StringBuilder(onBuild.note);
                onBuild.note = builder.append(note).toString();
            }
            return this;
        }

        public RespondTask build() {
            return onBuild;
        }
    }

}
