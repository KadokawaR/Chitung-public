package mirai.chitung.plugin.core.responder.help;

import com.sun.imageio.plugins.common.ImageUtil;
import mirai.chitung.plugin.administration.config.ConfigHandler;
import mirai.chitung.plugin.administration.config.FunctionConfig;
import mirai.chitung.plugin.core.groupconfig.GroupConfig;
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager;
import mirai.chitung.plugin.core.responder.RespondTask;
import mirai.chitung.plugin.core.responder.MessageResponder;
import mirai.chitung.plugin.utils.image.ImageCreater;
import mirai.chitung.plugin.utils.image.ImageSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Function implements MessageResponder<MessageEvent> {

    static final List<MessageType> types = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static class Position{
        int X;
        int Y;
        Position(int X,int Y){
            this.X=X;
            this.Y=Y;
        }
    }

    static Position[] positions = new Position[8];

    static{
        positions[1] = new Position(34,233); //responder
        positions[2] = new Position(34 ,1135); //mahjong
        positions[3] = new Position(285,146); //ir/ur
        positions[4] = new Position(286,395); //groupconfig
        positions[5] = new Position(286,933); //lottery
        positions[6] = new Position(536,145); //casino
        positions[7] = new Position(536,491); //fish
        //如下不是位置，是图片长宽
        positions[0] = new Position(788,1352);
    }

    static BufferedImage getImage(String path){
        try(InputStream is = Function.class.getResourceAsStream(path)){
            if(is==null) return null;
            return ImageIO.read(is);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    enum Type{
        Friend,
        Group
    }

    static FunctionConfig getRC(Type type){
        if (type == Type.Friend) {
            return ConfigHandler.getINSTANCE().config.getFriendFC();
        }
        return ConfigHandler.getINSTANCE().config.getGroupFC();
    }

    static GroupConfig getGC(long ID){
        return GroupConfigManager.getGroupConfig(ID);
    }

    static BufferedImage assemblePic(Type type,long groupID){

        String filePath = "/pics/function/help-0";

        BufferedImage[] images = new BufferedImage[8];
        String[] paths = new String[8];

        for(int i=0;i<paths.length;i++){
            paths[i] = filePath+i;
        }

        if(type==Type.Friend) {

            if (!getRC(type).isResponder()) {
                paths[1] += "-closed";
                paths[3] += "-closed";
            }

            paths[2] += "-closed";
            paths[4] += "-closed";
            paths[5] += "-closed";


            if (!getRC(type).isCasino() || !getRC(type).isGame()) {
                paths[6] += "-closed";
            }

            if (!getRC(type).isFish() || !getRC(type).isGame()) {
                paths[7] += "-closed";
            }

        } else {

            if (!getRC(type).isResponder()||!getGC(groupID).isResponder()) {
                paths[1] += "-closed";
                paths[3] += "-closed";
            }

            if (!getRC(type).isGame() || !getGC(groupID).isGame()) {
                paths[2] += "-closed";
            }

            if (!getRC(type).isLottery() || !getGC(groupID).isLottery()) {
                paths[5] += "-closed";
            }

            if (!getRC(type).isCasino() || !getRC(type).isGame() || !getGC(groupID).isCasino() || !getGC(groupID).isGame()) {
                paths[6] += "-closed";
            }

            if (!getRC(type).isFish() || !getRC(type).isGame() || !getGC(groupID).isFish() || !getGC(groupID).isGame()){
                paths[7] += "-closed";
            }

        }

        for(int i=0;i<images.length;i++){
            paths[i] += ".png";
        }

        for(int i=0;i<images.length;i++){
            images[i] = getImage(paths[i]);
        }

        BufferedImage result = images[0];

        for(int i=1;i<images.length;i++){
            assert images[0] != null;
            assert images[i] != null;
            result = ImageCreater.addImage(result, images[i], positions[i].X * images[0].getWidth()/positions[0].X, positions[i].Y * images[0].getHeight()/positions[0].Y);
        }

        return result;
    }


    @Override
    public boolean match(String content){
        return content.equals("/funct")|| content.equals("查看功能");
    }

    @Override
    public RespondTask handle(MessageEvent event){
        RespondTask.Builder builder = new RespondTask.Builder(event, this);
        if (match(event.getMessage().contentToString())){
            builder.addTask(()-> send(event));
        }
        return builder.build();
    }

    @Override
    public String getName() {
        return "功能";
    }

    public static void send(MessageEvent event){
        executor.schedule(new sendFunction(event),10, TimeUnit.MILLISECONDS);
    }

    static class sendFunction implements Runnable{

        private final MessageEvent event;
        sendFunction(MessageEvent event){
            this.event=event;
        }
        @Override
        public void run(){
            if(event instanceof GroupMessageEvent) {
                ImageSender.sendImageFromBufferedImage(event.getSubject(), assemblePic(Type.Group,((GroupMessageEvent) event).getGroup().getId()));
            } else {
                ImageSender.sendImageFromBufferedImage(event.getSubject(), assemblePic(Type.Friend,0L));
            }
        }
    }

    @NotNull
    @Override
    public List<MessageType> types() { return types; }
}
