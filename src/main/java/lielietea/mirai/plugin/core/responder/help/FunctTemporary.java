package lielietea.mirai.plugin.core.responder.help;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.core.responder.mahjong.FortuneTeller;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctTemporary implements MessageResponder<MessageEvent> {

    static final List<MessageType> types = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));

    @Override
    public boolean match(MessageEvent event){
        return event.getMessage().contentToString().equals("/funct")||event.getMessage().contentToString().equals("查看功能");
    }

    @Override
    public RespondTask handle(MessageEvent event){
        RespondTask.Builder builder = new RespondTask.Builder(event, this);
        if (match(event)){
            builder.addTask(()-> send(event.getSubject()));
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return "功能";
    }

    public static void send(Contact contact){
        final String functionPicPath = "/pics/help/Function.png";
        try (InputStream img = FortuneTeller.class.getResourceAsStream(functionPicPath)) {
            assert img != null;
            contact.sendMessage(Contact.uploadImage(contact, img));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public List<MessageType> types() { return types; }
}
