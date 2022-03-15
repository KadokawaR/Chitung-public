package lielietea.mirai.plugin.core.responder.feastinghelper.dinnerpicker;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 一个类似于”今天吃什么“的类
 * 会给用户推送随机加入3-10种配料的披萨
 */
public class MealPicker implements MessageResponder<MessageEvent> {

    static final List<MessageType> TYPES = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));
    static final Pattern REG_PATTERN = Pattern.compile("(/[Mm]eal)|(/[Dd]inner)|(/吃啥)|([oO][kK] [Mm]eal)|([oO][kK] [Dd]inner)|(((早饭)|(午饭)|(晚饭)|(夜宵)|(今天)|(今晚)|(早茶)|(宵夜))吃什么)");


    @Override
    public boolean match(MessageEvent event) {
        return REG_PATTERN.matcher(event.getMessage().contentToString()).matches();
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        return RespondTask.of(event, FoodCluster.reply(event, FoodCluster.Mode.COMMON), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }


    @Override
    public String getName() {
        return "吃什么";
    }
}
