package lielietea.mirai.plugin.utils;

import java.util.Calendar;
import java.util.Date;

public class StandardTimeUtil {
    /**
     * 获取当前时候后的最近的某个时间
     */
    public static Date getStandardFirstTime(int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = calendar.getTime();
        if (date.after(new Date())) calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取以毫秒为单位表示的时间长度
     */
    public static int getPeriodLengthInMS(int day, int hour, int min, int sec) {
        return (((day * 24 + hour) * 60 + min) * 60 + sec) * 1000;
    }
}