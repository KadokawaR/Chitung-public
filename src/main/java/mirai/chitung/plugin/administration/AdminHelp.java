package mirai.chitung.plugin.administration;

import mirai.chitung.plugin.utils.IdentityUtil;
import net.mamoe.mirai.event.events.MessageEvent;

public class AdminHelp {

    static final String ADMIN_HELP = "Bank：\n" +
            "/laundry 空格 金额：为自己增加/减少钱\n" +
            "/set 空格 QQ号 空格 钱：设置用户的钱的数量\n" +
            "/bank 空格 QQ号：查询用户的钱的数量\n\n" +
            "Broadcast:\n" +
            "/broadcast -f 或者 -g：进行好友或者群聊广播\n\n" +
            "Reset：\n" +
            "/reset 空格 ur：重置通用响应的配置文件\n" +
            "/reset 空格 ir：重置通用图库响应的配置文件\n" +
            "/reset 空格 config：重置 Config 配置文件\n\n" +
            "Blacklist：\n" +
            "/block 空格 -g 或者 -f 空格 QQ号：屏蔽该号码的群聊或者用户\n" +
            "/unblock 空格 -g 或者 -f 空格 QQ号：解除屏蔽该号码的群聊或者用户\n\n" +
            "Config：\n\n" +
            "/config -h：查看 config 的帮助\n" +
            "/config 空格 数字序号 空格 true/false：开关相应配置\n\n" +
            "Data：\n" +
            "/numf：查看好友数量\n" +
            "/numg：查看群聊数量\n" +
            "/coverage：查看总覆盖人数";

    public static void send(MessageEvent event){
        if(!IdentityUtil.isAdmin(event.getSender().getId())) return;
        if(event.getMessage().contentToString().equalsIgnoreCase("/adminhelp")) {
            event.getSubject().sendMessage(ADMIN_HELP);
        }
    }
}
