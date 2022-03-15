package lielietea.mirai.plugin.core.responder.feedback;

import com.google.common.collect.Lists;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.utils.MessageUtil;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FeedBack implements MessageResponder<MessageEvent> {
    static final List<MessageType> TYPES = Lists.newArrayList(MessageType.FRIEND);
    static FeedBack INSTANCE = new FeedBack();

    public static FeedBack getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public boolean match(MessageEvent event) {
        return event.getMessage().contentToString().contains("意见反馈");
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        RespondTask.Builder builder = new RespondTask.Builder(event, this);
        builder.addTask(() -> MessageUtil.notifyDevGroup("来自" + event.getSender().getId() + " - " + event.getSenderName() + "的反馈意见：\n\n" + event.getMessage().contentToString(),event.getBot().getId()));
        builder.addMessage("您的意见我们已经收到。");
        return builder.build();
    }

    @Override
    public String getName() {
        return "意见反馈";
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }

}
