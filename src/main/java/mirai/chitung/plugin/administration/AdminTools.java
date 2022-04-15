package mirai.chitung.plugin.administration;

import mirai.chitung.plugin.core.responder.ResponderManager;
import mirai.chitung.plugin.utils.IdentityUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.ArrayList;
import java.util.Iterator;

public class AdminTools {

    static final AdminTools INSTANCE = new AdminTools();

    public static AdminTools getINSTANCE() {
        return INSTANCE;
    }

    public void handleAdminCommand(MessageEvent event) {

        if (event.getMessage().contentToString().contains("/optimize")) {
            optimizeManually(event);
        }

        if (event.getMessage().contentToString().contains("/coverage")) {
            getCoverage(event);
        }

        if (event.getMessage().contentToString().contains("/numf")) {
            getFriendNum(event);
        }

        if (event.getMessage().contentToString().contains("/numg")) {
            getGroupNum(event);
        }

    }

    void optimizeManually(MessageEvent event) {
        MessageChainBuilder messages = new MessageChainBuilder();
        String result = ResponderManager.getINSTANCE().optimizeHandlerSequence(false);
        messages.append(result);
        event.getSubject().sendMessage(messages.build());
    }

    void getFriendNum(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            int size = event.getBot().getFriends().getSize();
            event.getSubject().sendMessage("七筒目前的好友数量是：" + size);
        }
    }

    void getGroupNum(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            int size = event.getBot().getGroups().getSize();
            event.getSubject().sendMessage("七筒目前的群数量是：" + size);
        }
    }

    void getCoverage(MessageEvent event) {
        if (IdentityUtil.isAdmin(event)) {
            Iterator<Group> listIter = event.getBot().getGroups().stream().iterator();
            ArrayList<Long> list = new ArrayList<>();
            while (listIter.hasNext()) {
                Iterator<NormalMember> listIterMember = listIter.next().getMembers().stream().iterator();
                while (listIterMember.hasNext()) {
                    long userID = listIterMember.next().getId();
                    if (!list.contains(userID)) {
                        list.add(userID);
                    }
                }
            }
            int size = list.size();
            event.getSubject().sendMessage("七筒目前的覆盖人数是：" + size);
            list.clear();
        }
    }

}
