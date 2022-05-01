package mirai.chitung.plugin.core.responder.lotterywinner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Touch;
import mirai.chitung.plugin.utils.fileutils.Write;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//TODO Need reimplement in Kotlin & Test
public class LotteryBummerExclusion {

    static String EXCLUSION_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator + "exclusion.json";
    //static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));

    LotteryBummerExclusion(){}

    private static final LotteryBummerExclusion INSTANCE;


    static class LotteryBummerExclusionClass{
        List<Long> userList;
        LotteryBummerExclusionClass(){
            this.userList = new ArrayList<>();
        }
    }

    static {
        INSTANCE = new LotteryBummerExclusion();
        initialize();
    }

    LotteryBummerExclusionClass exclusionClass;

    public static LotteryBummerExclusion getINSTANCE() {
        return INSTANCE;
    }

    static void initialize(){
        getINSTANCE().exclusionClass = new LotteryBummerExclusionClass();
        if(Touch.file(EXCLUSION_PATH)){
            try {
                getINSTANCE().exclusionClass = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(EXCLUSION_PATH)), StandardCharsets.UTF_8))), LotteryBummerExclusionClass.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            writeRecord();
        }
    }

    static LotteryBummerExclusionClass readRecord(){
        try {
            return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(EXCLUSION_PATH)), StandardCharsets.UTF_8))), LotteryBummerExclusionClass.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void writeRecord(){
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(getINSTANCE().exclusionClass);
        Write.cover(jsonString, EXCLUSION_PATH);
        getINSTANCE().exclusionClass=readRecord();
    }

    public boolean match(String content) {
        return content.equalsIgnoreCase("/open bummer")|| content.equalsIgnoreCase("/close bummer");
    }

    /*public RespondTask handle(MessageEvent event) {

        boolean isOpen = event.getMessage().contentToString().contains("/open");

        if(isOpen) {
            if (!getINSTANCE().exclusionClass.userList.contains(event.getSender().getId())) getINSTANCE().exclusionClass.userList.add(event.getSender().getId());
            writeRecord();
            if(event instanceof GroupMessageEvent){
                return RespondTask.of(event,new At(event.getSender().getId()).plus("已为您打开Bummer保护。"), this);
            } else {
                return RespondTask.of(event,"已为您打开Bummer保护。", this);
            }
        } else {

            if (!getINSTANCE().exclusionClass.userList.contains(event.getSender().getId())){
                if(event instanceof GroupMessageEvent){
                    return RespondTask.of(event,new At(event.getSender().getId()).plus("您没有开启Bummer保护。"), this);
                } else {
                    return RespondTask.of(event,"您没有开启Bummer保护。", this);
                }
            }

            getINSTANCE().exclusionClass.userList.remove(event.getSender().getId());
            writeRecord();

            if(event instanceof GroupMessageEvent){
                return RespondTask.of(event,new At(event.getSender().getId()).plus("已为您关闭Bummer保护。"), this);
            } else {
                return RespondTask.of(event,"已为您关闭Bummer保护。", this);
            }
        }
    }*/

    public String getName() {
        return "BummerExclusion";
    }
}
