package mirai.chitung.plugin.core.harbor;

// 预设好的 PortRequestInfo
public class PortRequestInfos {
    // PERSONAL 用于限制响应来自特定用户的消息的次数，无论该消息是通过私聊还是群消息发出。阈值以 Bot 回复消息数量为准。
    public final static PortRequestInfo PERSONAL = new PortRequestInfo("personal", 5, 100);
    final static int NOT_LIMITED = 99999;
    // GROUP_MINUTE 用于限制每分钟响应来自群的消息的次数。阈值以 Bot 回复消息数量为准。
    public final static PortRequestInfo GROUP_MINUTE = new PortRequestInfo("group", 10, NOT_LIMITED);
    // TOTAL_DAILY 用于限制每天响应所有来源的消息的次数。阈值以 Bot 回复消息数量为准。
    // TOTAL_DAILY 的 Port Record(Threshold Record) 存储在 id 0 下。
    public final static PortRequestInfo TOTAL_DAILY = new PortRequestInfo("total", NOT_LIMITED, 4000);
}
