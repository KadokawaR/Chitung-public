package lielietea.mirai.plugin.core.responder.feastinghelper.drinkpicker;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 一个类似于”今天吃什么“的类
 *
 * <p>用{@link DrinkPicker#getPersonalizedHourlyDrink(MessageEvent)}来获取根据用户而变化的Hourly Random Drink</p>
 */
@Deprecated
public class DrinkPicker implements MessageResponder<MessageEvent> {
    static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static final List<Pattern> REG_PATTERN = new ArrayList<>();

    static final ArrayList<String> DRINK_BASE = new ArrayList<>(Arrays.asList(
            "铁观音奶茶",
            "大红袍奶茶",
            "四季奶青",
            "柠檬养乐多",
            "芒果杨枝甘露",
            "多肉葡萄",
            "多肉莓莓",
            "芝芝莓莓",
            "草莓奶昔",
            "巧克力奶昔",
            "幽兰拿铁",
            "四季春茶",
            "乌龙奶茶",
            "抹茶鲜牛乳",
            "奶绿",
            "冻顶乌龙茶",
            "茉莉绿茶",
            "红茶玛奇朵",
            "阿华田",
            "红茶拿铁",
            "绿茶拿铁",
            "三季虫",
            "豆乳米麻薯",
            "血糯米奶茶",
            "茉莉奶绿",
            "桂花酒酿奶茶",
            "黄金椰椰奶茶",
            "双皮奶奶茶",
            "西瓜啵啵",
            "桂花龙眼冰",
            "奥利奥蛋糕奶茶",
            "杨枝甘露",
            "百香凤梨",
            "手捣芒果绿",
            "老红糖奶茶",
            "海盐抹茶芝士",
            "阿华田",
            "人间烟火",
            "芊芊马卡龙",
            "桂花弄",
            "筝筝纸鸳",
            "烟火易冷",
            "素颜锡兰",
            "不知冬",
            "声声乌龙",
            "凤栖绿桂",
            "栀晓",
            "草莓桃桃",
            "硫酸铜溶液"

    ));

    static final ArrayList<String> TOPPING = new ArrayList<>(Arrays.asList(
            "加珍珠",
            "加波霸",
            "加布丁",
            "加龟苓",
            "加脆波波",
            "加寒天",
            "加椰果",
            "加红豆",
            "加三兄弟",
            "加咖啡冻",
            "加冰激凌"
    ));

    static final ArrayList<String> SUGAR_LEVEL = new ArrayList<>(Arrays.asList(
            "全糖",
            "半糖",
            "三分糖",
            "无糖",
            "七分糖",
            "少糖",
            "少少糖",
            "少少少糖",
            "不额外加糖"
    ));

    static {
        {
            REG_PATTERN.add(Pattern.compile(".*" + "喝点什么" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "奶茶" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "喝了什么" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "喝什么" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "有点渴" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "好渴" + ".*"));
            REG_PATTERN.add(Pattern.compile(".*" + "来一杯" + ".*"));
        }
    }

    @Override
    public boolean match(MessageEvent event) {
        for (Pattern pattern : REG_PATTERN) {
            if (pattern.matcher(event.getMessage().contentToString()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        return RespondTask.of(event, "您的饮品是 " + getPersonalizedHourlyDrink(event), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }


    //获取每小时变化的，根据用户而不同的随机饮品
    static String getPersonalizedHourlyDrink(MessageEvent event) {
        return mixDrink(pickPersonalizedHourlyIngredients(event.getSender().getId()));
    }

    static int[] pickPersonalizedHourlyIngredients(long qqID) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);//获得当前时间
        long fullDate = year + month * 10000 + date * 1000000 + hour * 100000000L;//用时间和小时构成一个10位数
        long getSixNum = fullDate * 1000000L / qqID % 1000000L;//除以QQ号之后获得这个数的最后六位
        long firstTwoNum = getSixNum / 10000;
        long middleTwoNum = (getSixNum % 10000) / 100;
        long lastTwoNum = getSixNum % 100; //获得这个数的三组两位数；

        int[] randomTea = new int[3]; //定义返回数组
        randomTea[0] = Math.toIntExact(firstTwoNum % DRINK_BASE.size());
        randomTea[1] = Math.toIntExact(middleTwoNum % TOPPING.size());
        randomTea[2] = Math.toIntExact(lastTwoNum % SUGAR_LEVEL.size());
        return randomTea;
    }

    static String mixDrink(int[] randomTea) {
        return DRINK_BASE.get(randomTea[0]) + TOPPING.get(randomTea[1]) + SUGAR_LEVEL.get(randomTea[2]);
    }

    @Override
    public String getName() {
        return "奶茶";
    }
}
