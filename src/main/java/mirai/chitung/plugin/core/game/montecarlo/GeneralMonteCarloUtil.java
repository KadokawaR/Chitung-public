package mirai.chitung.plugin.core.game.montecarlo;

import mirai.chitung.plugin.core.bank.PumpkinPesoWindow;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.regex.Pattern;

public class GeneralMonteCarloUtil {

    static final String NotRightBetNumber = "/bet 指令不正确，请重新再尝试";
    static final String YouDontHaveEnoughMoney = "操作失败，请检查您的南瓜比索数量。";

    //返回下注的钱，不行返回null
    public static Integer getBet(String message) {
        message = message.replace(" ", "");
        String ID = Pattern.compile("[^0-9]").matcher(message).replaceAll(" ").trim();
        try {
            return Integer.parseInt(ID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkBet(MessageEvent event, String content, int original) {
        //判定数值是否正确
        Integer bet = null;
        try {
            bet = getBet(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bet == null) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return false;
        }

        if (bet <= 0) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return false;
        }

        if (bet > 999999) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(NotRightBetNumber);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return false;
        }

        if(original>0) bet+=original;
        //判定账户里是否有钱
        if (!hasEnoughMoney(event, bet)) {
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append(YouDontHaveEnoughMoney);
            event.getSubject().sendMessage(mcb.asMessageChain());
            return false;
        }
        return true;
    }

    //判定是否有钱
    public static boolean hasEnoughMoney(MessageEvent event, int bet) {
        return PumpkinPesoWindow.hasEnoughMoney(event, bet);
    }

    //是不是群聊
    public static boolean isGroupMessage(MessageEvent event) {
        return (event.getClass().equals(GroupMessageEvent.class));
    }

    //给群聊的消息前面加AT
    public static MessageChainBuilder mcbProcessor(MessageEvent event) {
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (isGroupMessage(event)) {
            mcb.append((new At(event.getSender().getId()))).append(" ");
        }
        return mcb;
    }
}
