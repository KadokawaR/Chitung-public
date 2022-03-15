package lielietea.mirai.plugin.core.responder;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashMap;
import java.util.Map;

public class ResponderCenter {
    Map<Bot, ResponderTaskDistributor> MULTI_BOT_DISTRIBUTOR_MAP = new HashMap<>();

    static ResponderCenter INSTANCE = new ResponderCenter();

    public static ResponderCenter getINSTANCE() {
        return INSTANCE;
    }

    public void handleMessage(MessageEvent event) {
        if(MULTI_BOT_DISTRIBUTOR_MAP.containsKey(event.getBot()))
            MULTI_BOT_DISTRIBUTOR_MAP.get(event.getBot()).handleMessage(event);
        else{
            MULTI_BOT_DISTRIBUTOR_MAP.put(event.getBot(),new ResponderTaskDistributor(event.getBot()));
            MULTI_BOT_DISTRIBUTOR_MAP.get(event.getBot()).handleMessage(event);
        }
    }

    public void close(){
        MULTI_BOT_DISTRIBUTOR_MAP.values().forEach(ResponderTaskDistributor::close);
    }
}
