package lielietea.mirai.plugin.core.responder.namecardshuffle;

import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//TODO 这玩意太恐怖了，先不上了
public class ShuffleNameCard {
    public static List<String> getNameCardList(Iterator<NormalMember> contactList){
        List<String> nameCardList = new ArrayList<>();
        while (contactList.hasNext()){
            assert false;
            nameCardList.add(contactList.next().getNameCard());
        }
        return nameCardList;
    }

    public static List<String> shuffleList(List<String> nameCardList){
        Collections.shuffle(nameCardList);
        return nameCardList;
    }

    public static void shuffleMain(GroupMessageEvent event){
        Iterator<NormalMember> contactList = event.getGroup().getMembers().stream().iterator();
        List<String> nameCardListShuffled = shuffleList(getNameCardList(contactList));
        int index = 0;
        //todo 检测两边list抓取的数量是否不对，以及不确定getNameCard没有群名片的是直接获得昵称还是返回空
        while (contactList.hasNext()){
            contactList.next().setNameCard(nameCardListShuffled.get(index));
            index += 1;
        }
    }

    public static void run(GroupMessageEvent event){
        if (event.getMessage().contentToString().equals("/shuffle")){
            if (botPermissionChecker.check(event)){
                shuffleMain(event);
            } else {
                event.getGroup().sendMessage("七筒暂时没有管理员权限，请授予七筒管理员权限。");
            }

        }
    }

    static class botPermissionChecker {
        public static boolean check(GroupMessageEvent event) {
            return ((event.getGroup().getBotPermission().equals(MemberPermission.ADMINISTRATOR)) || (event.getGroup().getBotPermission().equals(MemberPermission.OWNER)));
        }
    }
}
