package lielietea.mirai.plugin.core.responder.basic;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.*;

public class Repeater {

    static Map<Long,List<MessageChain>> messageContainer = new HashMap<>();

    public static void handle(GroupMessageEvent event){
        if(!messageContainer.containsKey(event.getGroup().getId())){
            List<MessageChain> temp = new ArrayList<MessageChain>() {{add(event.getMessage());}};
            messageContainer.put(event.getGroup().getId(),temp);
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==0){
            messageContainer.get(event.getGroup().getId()).add(event.getMessage());
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==1){
            if(!messageContainer.get(event.getGroup().getId()).get(0).equals(event.getMessage())){
                messageContainer.get(event.getGroup().getId()).clear();
            }
            messageContainer.get(event.getGroup().getId()).add(event.getMessage());
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==2){
            MessageChain m1 = messageContainer.get(event.getGroup().getId()).get(0);
            MessageChain m2 = messageContainer.get(event.getGroup().getId()).get(0);
            MessageChain m3 = event.getMessage();
            if(m1.equals(m2)&&m2.equals(m3)){
                event.getGroup().sendMessage(m3);
            }
            messageContainer.get(event.getGroup().getId()).clear();
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()>2){
            messageContainer.get(event.getGroup().getId()).clear();
        }
    }

}
