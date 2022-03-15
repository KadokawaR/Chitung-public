package lielietea.mirai.plugin.core.responder.furrygamesindex;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.utils.image.ImageSender;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FurryGamesSearch implements MessageResponder<MessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));

    @Override
    public boolean match(MessageEvent event) {
        return (event.getMessage().contentToString().contains("/fgi ")||event.getMessage().contentToString().contains("/FGI "))&&!event.getMessage().contentToString().toLowerCase().contains("/fgi random");
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        RespondTask.Builder builder = new RespondTask.Builder(event,this);
        String givenGameName = event.getMessage().contentToString().replace("/fgi ","").replace("/FGI ","");
        StringBuilder sb = new StringBuilder();

        String[] gameInfo = FurryGamesIndex.getGameInfo(givenGameName,false);
        if (gameInfo==null){
            builder.addMessage("没有找到对应的游戏信息。");
        } else {
            if (gameInfo[2] == null){
                builder.addMessage("该游戏没有相关描述。");
            } else {
                sb.append(gameInfo[0])
                        .append("\n\n")
                        .append(gameInfo[2])
                        .append("\n\n")
                        .append(gameInfo[1]);
                builder.addMessage(sb.toString());
            }
            if (gameInfo[3]!=null){
                builder.addTask(()->{
                    URL url = null;
                    try {
                        url = new URL(gameInfo[3]);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    ImageSender.sendImageFromURL(event.getSubject(),url);
                });
            }
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return "FGI Search";
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }
}
