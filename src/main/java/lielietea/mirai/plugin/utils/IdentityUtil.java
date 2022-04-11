package lielietea.mirai.plugin.utils;

import com.google.common.collect.ImmutableSet;
import lielietea.mirai.plugin.administration.config.ConfigHandler;
import lielietea.mirai.plugin.core.groupconfig.GroupConfigManager;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentityUtil {
    static final Set<Long> botList = ImmutableSet.of(
            3628496803L, //七筒#2
            2429465624L, //七筒#3
            3582637350L,//七筒#4
            1256252623L,//七筒#5
            3621269439L, //wymTbot 维护者 1875018140
            736951095L, //未知
            1528805494L, //龙龙
            1661492751L, //贾维斯
            3295045384L, //完犊子BOT
            1417324212L,
            3270864281L,//nulqwerty 维护者 1417324298
            120213813L, //KizuBot
            3028159740L, //RPGmirai 维护者 173799682
            2513882944L //KaitoBot 维护者 435907629
    );

    static final Set<Long> developerList = ImmutableSet.of(
            2955808839L, //KADOKAWA
            1811905537L //MARBLEGATE
    );

    static final Set<Long> adminList = new HashSet<Long>(){{addAll(ConfigHandler.getINSTANCE().config.getAdminID());addAll(developerList);}};

    public static boolean isBot(long id){
        return isUnofficialBot(id) || isOfficialBot(id);
    }

    public static boolean isUnofficialBot(long id){
        return botList.contains(id);
    }

    public static boolean isOfficialBot(long id){
        return String.valueOf(id).startsWith("28541963") && id / 1000000000L >= 1;
    }

    public static boolean isBot(MessageEvent event){
        return isBot(event.getSender().getId());
    }

    public static boolean isAdmin(long id){
        return adminList.contains(id);
    }

    public static boolean isAdmin(MessageEvent event){
        return isAdmin(event.getSender().getId());
    }

    public static List<Long> getDevGroup(){
        return ConfigHandler.getINSTANCE().config.getDevGroupID();
    }

    public static boolean isDevGroup(long groupID){
        return getDevGroup().contains(groupID);
    }

}
