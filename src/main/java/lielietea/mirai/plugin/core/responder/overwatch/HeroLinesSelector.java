package lielietea.mirai.plugin.core.responder.overwatch;


import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class HeroLinesSelector implements MessageResponder<GroupMessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Collections.singletonList(MessageType.GROUP));
    static final List<Pattern> REG_PATTERN = new ArrayList<>();

    static {
        {
            REG_PATTERN.add(Pattern.compile("/大招"));
            REG_PATTERN.add(Pattern.compile("/英雄不朽"));
        }
    }

    @Override
    public boolean match(GroupMessageEvent event) {
        for (Pattern pattern : REG_PATTERN) {
            if (pattern.matcher(event.getMessage().contentToString()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RespondTask handle(GroupMessageEvent event) {
        return RespondTask.of(event, HeroLinesCluster.pickUltimateAbilityHeroLineByRandomHero(), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }

    @Override
    public String getName() {
        return "守望先锋大招台词";
    }
}
