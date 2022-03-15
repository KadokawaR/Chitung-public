package lielietea.mirai.plugin.core.responder.basic;


import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class AntiDirtyWord implements MessageResponder<MessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static final List<Pattern> REG_PATTERN = new ArrayList<>();

    static {
        {
            REG_PATTERN.add(Pattern.compile(".*" + "([日干操艹草滚])([你尼泥])([妈马麻])" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "([Mm])otherfucker" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "([Ff])uck ([Yy])ou" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "([Ff])uck" + ".*"));
        }
    }

    @Override
    public boolean match(MessageEvent event) {
        for (Pattern pattern : REG_PATTERN) {
            if (pattern.matcher(event.getMessage().contentToString()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        return RespondTask.of(event, AutoReplyLinesCluster.reply(AutoReplyLinesCluster.ReplyType.ANTI_DIRTY_WORDS), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }


    @Override
    public String getName() {
        return "自动回复：反脏话";
    }
}
