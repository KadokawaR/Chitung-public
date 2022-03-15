package lielietea.mirai.plugin.core.responder.dice;

import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import lielietea.mirai.plugin.utils.exception.NoHandlerMethodMatchException;
import net.mamoe.mirai.event.events.MessageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayDice implements MessageResponder<MessageEvent> {
    static final List<Pattern> REG_PATTERN = new ArrayList<>();
    static final Pattern PATTERN_COMMON_COMMAND = Pattern.compile("(/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7})");
    static final Pattern PATTERN_DND = Pattern.compile("\\.([1-9]\\d{0,2})([dD])[1-9]\\d{0,7}");
    static final Pattern PATTERN_DND_SINGLE_ROLL = Pattern.compile("\\.([dD])[1-9]\\d{0,7}");
    static final Pattern CAPTURE_PATTERN_COMMON_COMMAND = Pattern.compile("(/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7})");
    static final Pattern CAPTURE_PATTERN_DND = Pattern.compile("\\.([1-9]\\d{0,2})([dD])([1-9]\\d{0,7})");
    static final Pattern CAPTURE_PATTERN_DND_SINGLE_ROLL = Pattern.compile("\\.([dD])([1-9]\\d{0,7})");

    static {
        {
            REG_PATTERN.add(Pattern.compile("(/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7})"));
            REG_PATTERN.add(Pattern.compile("\\.([1-9]\\d{0,2})([dD])[1-9]\\d{0,7}"));
            REG_PATTERN.add(Pattern.compile("\\.([dD])[1-9]\\d{0,7}"));
        }
    }

    static final List<MessageType> type = new ArrayList<>(Arrays.asList(MessageType.FRIEND, MessageType.GROUP));

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
        return RespondTask.of(event, executeDiceCommand(event), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return type;
    }


    static String executeDiceCommand(MessageEvent event) {
        if (PATTERN_COMMON_COMMAND.matcher(event.getMessage().contentToString()).matches()) {
            return DiceFactory.getCustomDice(captureFromPatternCommon(event.getMessage().contentToString()), 1).buildMessage();
        } else {
            if (PATTERN_DND.matcher(event.getMessage().contentToString()).matches()) {
                return DiceFactory.getCustomDice(captureFromPatternDND(event.getMessage().contentToString()).get(1), captureFromPatternDND(event.getMessage().contentToString()).get(0)).buildMessage();
            } else if (PATTERN_DND_SINGLE_ROLL.matcher(event.getMessage().contentToString()).matches()) {
                return DiceFactory.getCustomDice(captureFromPatternDNDSingleRoll(event.getMessage().contentToString()), 1).buildMessage();
            }
        }
        throw new NoHandlerMethodMatchException("匹配骰子", event);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static int captureFromPatternCommon(String input) {
        Matcher matcher = CAPTURE_PATTERN_COMMON_COMMAND.matcher(input);
        matcher.find();
        return Integer.parseInt(matcher.group(2));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static List<Integer> captureFromPatternDND(String input) {
        Matcher matcher = CAPTURE_PATTERN_DND.matcher(input);
        matcher.find();
        List<Integer> captured = new ArrayList<>();

        captured.add(Integer.valueOf(matcher.group(1)));
        captured.add(Integer.valueOf(matcher.group(3)));

        return captured;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static int captureFromPatternDNDSingleRoll(String input) {
        Matcher matcher = CAPTURE_PATTERN_DND_SINGLE_ROLL.matcher(input);
        matcher.find();
        return Integer.parseInt(matcher.group(2));
    }

    @Override
    public String getName() {
        return "掷骰子";
    }
}