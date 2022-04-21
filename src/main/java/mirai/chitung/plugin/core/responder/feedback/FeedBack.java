package mirai.chitung.plugin.core.responder.feedback;

import mirai.chitung.plugin.utils.MessageUtil;
import com.google.common.collect.Lists;
import mirai.chitung.plugin.core.responder.MessageResponder;
import mirai.chitung.plugin.core.responder.RespondTask;
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
    public boolean match(String content) {
        return content.contains("意见反馈");
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        RespondTask.Builder builder = new RespondTask.Builder(event, this);
        builder.addTask(() -> MessageUtil.notifyDevGroup("来自" + event.getSender().getId() + " - " + event.getSenderName() + "的反馈意见：\n\n" + event.getMessage().contentToString(),event.getBot()));
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
