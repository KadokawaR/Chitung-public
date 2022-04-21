package mirai.chitung.plugin.core.responder.repeater;

import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.util.*;

public class Repeater {

    static Map<Long,List<String>> messageContainer = new HashMap<>();

    public static void handle(GroupMessageEvent event){

        if(event.getMessage().serializeToMiraiCode().contains("mirai")) {
            messageContainer.get(event.getGroup().getId()).clear();
            return;
        }

        String message = event.getMessage().contentToString();

        if(message.equals("")){
            messageContainer.get(event.getGroup().getId()).clear();
            return;
        }

        if(!messageContainer.containsKey(event.getGroup().getId())){
            List<String> temp = new ArrayList<String>() {{add(message);}};
            messageContainer.put(event.getGroup().getId(),temp);
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==0){
            messageContainer.get(event.getGroup().getId()).add(message);
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==1){
            if(!messageContainer.get(event.getGroup().getId()).get(0).equals(message)){
                messageContainer.get(event.getGroup().getId()).clear();
            }
            messageContainer.get(event.getGroup().getId()).add(message);
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()==2){
            String m1 = messageContainer.get(event.getGroup().getId()).get(0);
            String m2 = messageContainer.get(event.getGroup().getId()).get(1);

            if(m1.equals(m2)&&m2.equals(message)){
                event.getGroup().sendMessage(message);
            }
            messageContainer.get(event.getGroup().getId()).clear();
            return;
        }

        if(messageContainer.get(event.getGroup().getId()).size()>2){
            messageContainer.get(event.getGroup().getId()).clear();
        }
    }

}
