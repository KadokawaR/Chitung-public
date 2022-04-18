package mirai.chitung.plugin.core.harbor;

public class PortRequestInfo {
    final String tag;
    final int minute_limit;
    final int daily_limit;

    public PortRequestInfo(String tag, int minute_limit, int daily_limit) {
        this.tag = tag;
        this.minute_limit = minute_limit;
        this.daily_limit = daily_limit;
    }
}
