package lielietea.mirai.plugin.core.responder.dice;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class CommonDice {
    final int bound;
    final int repeat;
    List<Integer> result;

    CommonDice(int bound, int repeat) {
        this.bound = bound;
        this.repeat = repeat;
        result = new ArrayList<>();
    }

    public static CommonDice getInstance(int bound, int repeat) {
        CommonDice dice = new CommonDice(bound, repeat);
        dice.reroll();
        return dice;
    }

    /**
     * 扔骰子操作，用这个方法来重新扔一次骰子
     */
    public void reroll() {
        if (!result.isEmpty()) result = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            result.add(new Random(System.nanoTime()).nextInt(bound) + 1);
        }
    }

    /**
     * 返回一个结果组成的List
     *
     * @return 一个包含了投掷结果的ArrayList
     */
    public List<Integer> toList() {
        return result;
    }


    public String buildMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("您掷出的点数是:");
        result.forEach(result -> builder.append(+result).append(" "));
        return builder.toString();
    }


    @Override
    public String toString() {
        return "CommonDice{" +
                "bound=" + bound +
                ", repeat=" + repeat +
                ", result=" + result +
                '}';
    }
}
