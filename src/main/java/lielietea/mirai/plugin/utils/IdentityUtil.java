package lielietea.mirai.plugin.utils;

import com.google.common.collect.ImmutableSet;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Set;

public class IdentityUtil {
    static final Set<Long> botList = ImmutableSet.of(
            3628496803L, //七筒#2
            2429465624L, //七筒#3
            3621269439L, //wymTbot 维护者 1875018140
            736951095L, //未知
            1528805494L, //啥龙龙
            1661492751L, // 贾维斯
            3295045384L, //完犊子BOT
            1417324212L,
            3270864281L,//nulqwerty 维护者 1417324298
            120213813L //KizuBot
    );

    static final Set<Long> adminList = ImmutableSet.of(
            2955808839L, //KADOKAWA
            1811905537L, //MARBLEGATE
            493624254L   //感谢咕咕科提供账号
    );

    static final Set<Long> unusedBotList = ImmutableSet.of(
            340865180L
    );

    public static boolean isBot(long id){
        return botList.contains(id);
    }

    public static boolean isBot(MessageEvent event){
        return isBot(event.getSender().getId());
    }

    public static boolean isAdmin(long id){
        return adminList.contains(id);
    }

    public static boolean containsUnusedBot(Group group){
        for(Long ID:unusedBotList){
            for(NormalMember nm:group.getMembers()){
                if(nm.getId()==ID) return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(MessageEvent event){
        return isAdmin(event.getSender().getId());
    }

    public enum DevGroup{
        DEFAULT(578984285L);

        long groupId;

        DevGroup(long groupId) {
            this.groupId = groupId;
        }

        public long getID(){
            return groupId;
        }

        public boolean isDevGroup(long ID){
            for(DevGroup dg:DevGroup.values()){
                if (ID==dg.getID()) return true;
            }
            return false;
        }
    }
}
