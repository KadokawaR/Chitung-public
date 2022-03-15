package lielietea.mirai.plugin.core.responder.dice;

/**
 * 这个类用来获取各类骰子
 */
public class DiceFactory {
    public static CommonDice getCommonDice() {
        return CommonDice.getInstance(6, 1);
    }

    public static CommonDice getCoin() {
        return CommonDice.getInstance(2, 1);
    }

    public static CommonDice getDNDDice() {
        return CommonDice.getInstance(100, 1);
    }

    public static CommonDice getCOCDice() {
        return getDNDDice();
    }

    public static CommonDice getCustomDice(int bound, int repeat) {
        return CommonDice.getInstance(bound, repeat);
    }
}
