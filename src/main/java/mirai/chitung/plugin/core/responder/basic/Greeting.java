package mirai.chitung.plugin.core.responder.basic;

import mirai.chitung.plugin.core.responder.RespondTask;
import mirai.chitung.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


public class Greeting implements MessageResponder<GroupMessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Collections.singletonList(MessageType.GROUP));
    static final Pattern REG_PATTERN = Pattern.compile("[Hh]((ello)|(i))");
    static final List<String> REPLIES = new ArrayList<>();
    static final Random rand = new Random();

    static {
        {
            REPLIES.add("Hi");
            REPLIES.add("Hello");
            REPLIES.add("Hey");
        }
    }

    @Override
    public boolean match(String content) {
        return REG_PATTERN.matcher(content).matches();
    }

    @Override
    public RespondTask handle(GroupMessageEvent event) {
        return RespondTask.of(event, REPLIES.get(rand.nextInt(REPLIES.size())), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }

    @Override
    public String getName() {
        return "自动回复：打招呼";
    }
}
