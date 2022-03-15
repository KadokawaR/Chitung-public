package lielietea.mirai.plugin.utils;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NudgeEvent;

public class Nudge {
    public static void returnNudge(NudgeEvent event){
        if(IdentityUtil.isBot(event.getFrom().getId())) return;
        if(!(event.getSubject() instanceof Group)) return;
        if (event.getTarget().equals(event.getBot())){
            event.getFrom().nudge().sendTo(event.getSubject());
            event.getSubject().sendMessage("啥事？");
        }
    }

    public static void mentionNudge(GroupMessageEvent event){
        if(IdentityUtil.isBot(event.getSender().getId())) return;
        if (event.getMessage().contentToString().contains(String.valueOf(event.getBot().getId()))){
            event.getSender().nudge().sendTo(event.getSubject());
        }
    }
}
