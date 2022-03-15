package lielietea.mirai.plugin.core.game.montecarlo.blackjack;

import lielietea.mirai.plugin.core.bank.PumpkinPesoWindow;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.data.BlackJackData;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.data.BlackJackPlayer;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.enums.BlackJackOperation;
import lielietea.mirai.plugin.core.game.montecarlo.blackjack.enums.Color;
import lielietea.mirai.plugin.utils.IdentityUtil;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.*;

public class BlackJackUtils {

    static final int CARD_NUMBER = 4;

    //判定消息里面是否有触发关键词
    public static boolean isBlackJack(MessageEvent event){
        return event.getMessage().contentToString().equals("/blackjack")||event.getMessage().contentToString().equals("二十一点");
    }

    //判定是否是下注
    public static boolean isBet(MessageEvent event){
        return event.getMessage().contentToString().contains("/bet")||event.getMessage().contentToString().contains("下注");
    }

    //判定是哪种操作
    public static BlackJackOperation bjOperation(MessageEvent event){
        switch(event.getMessage().contentToString()){
            case "/assurance":
            case "/Assurance":
            case "买保险":
                System.out.println("Assurance");
                return BlackJackOperation.Assurance;
            case "/deal":
            case "/Deal":
            case "要牌":
                System.out.println("Deal");
                return BlackJackOperation.Deal;
            case "/double":
            case "/Double":
            case "双倍下注":
                System.out.println("Double");
                return BlackJackOperation.Double;
            case "/fold":
            case "/Fold":
            case "停牌":
                System.out.println("Fold");
                return BlackJackOperation.Fold;
            case "/pair":
            case "/Pair":
            case "下注对子":
                System.out.println("Pair");
                return BlackJackOperation.Pair;
            case "/split":
            case "/Split":
            case "分牌":
                System.out.println("Split");
                return BlackJackOperation.Split;
            case "/surrender":
            case "/Surrender":
            case "投降":
                System.out.println("Surrender");
                return BlackJackOperation.Surrender;
        }
        return null;
    }

    //返回下注的钱，不行返回null
    public static Integer getBet(MessageEvent event){
        String message = event.getMessage().contentToString();
        message = message.replace(" ","");
        if (message.contains("/bet")){
            message = message.replace("/bet","");
        }
        if (message.contains("下注")){
            message = message.replace("下注","");
        }
        try {
            return Integer.parseInt(message);
        }
        catch(NumberFormatException e){
            e.printStackTrace();
        }
        return null;
    }

    //生成四幅扑克牌组成的序列
    public static List<Integer> createPokerPile(){
        List<Integer> list = new ArrayList<>();
        for(int i=1;i<=52*CARD_NUMBER;i++){ list.add(i); }
        Collections.shuffle(list);
        return list;
    }

    //获得扑克牌的花色
    public static Color getColor(Integer integer){
        return Color.values()[integer%4];
    }

    //获得扑克牌的数字
    public static String getNumber(Integer integer){
        int number = integer%13;
        if (number<=10 && number>=2) return String.valueOf(number);
        switch (number){
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 0: return "K";
        }
        return null;
    }

    //组合成扑克牌字符串
    public static String getPoker(Integer integer){
        String poker = "";
        switch(getColor(integer)){
            case Club: poker += "♣"; break;
            case Heart: poker += "♥"; break;
            case Spade: poker += "♠"; break;
            case Diamond: poker += "♦"; break;
        }
        return poker+getNumber(integer);
    }

    //查看列表里是否有相应号码
    public static boolean isInTheList(MessageEvent event,List<BlackJackData> globalData){
        if (globalData.isEmpty()) return false;
        for (BlackJackData bjd : globalData){
            if (bjd.getID()==event.getSubject().getId()){
                return true;
            }
        }
        return false;
    }

    //查看全局列表里是几号
    public static Integer indexInTheList(MessageEvent event,List<BlackJackData> globalData){
        int index = 0;
        for (BlackJackData bjd : globalData){
            if (bjd.getID()==event.getSubject().getId()){
                return index;
            }
            index += 1;
        }
        return null;
    }

    //判定是否有钱
    public static boolean hasEnoughMoney(MessageEvent event, int bet){
        return PumpkinPesoWindow.hasEnoughMoney(event,bet);
    }

    //查看用户在列表里第几个
    public static Integer indexOfThePlayer(List<BlackJackPlayer> blackJackPlayerList, long ID){
        int index = 0;
        for (BlackJackPlayer bjp : blackJackPlayerList){
            if (bjp.getID()==ID){
                return index;
            }
            index += 1;
        }
        return null;
    }

    //查看用户是否在该群的列表里
    public static boolean playerIsInTheList(List<BlackJackPlayer> blackJackPlayerList, long ID){
        for (BlackJackPlayer bjp : blackJackPlayerList){
            if (bjp.getID()==ID){
                return true;
            }
        }
        return false;
    }

    //是不是群聊
    public static boolean isGroupMessage(MessageEvent event){
        return (event.getClass().equals(GroupMessageEvent.class));
    }

    //给群聊的消息前面加AT
    public static MessageChainBuilder mcbProcessor(MessageEvent event){
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (isGroupMessage(event)){
            mcb.append((new At(event.getSender().getId()))).append(" ");
        }
        return mcb;
    }

    //管理员工具
    public static void adminToolsInBlackJack(MessageEvent event){
        if (!IdentityUtil.isAdmin(event.getSender().getId())) return;
        if (event.getMessage().contentToString().equals("/endbet")){
            BlackJack.endBetActivity(event);
        }
        if(event.getMessage().contentToString().equals("/cardpile")){
            StringBuilder res = new StringBuilder();
            for (int i =0;i<15;i++){
                if (isGroupMessage(event)){
                    res.append(getPoker(BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getCardPile().get(i))).append(" ");
                } else {
                    res.append(getPoker(BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getCardPile().get(i))).append(" ");
                }

            }
            event.getSubject().sendMessage(res.toString());
        }
        if(event.getMessage().contentToString().equals("/endoperation")){
            BlackJack.foldEveryoneInSixtySeconds(event);
        }

        if(event.getMessage().contentToString().equals("/adminassurance")){
            if(isGroupMessage(event)){
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(0,1);
            } else {
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(0,1);
            }
            event.getSubject().sendMessage("测试工具：已设置庄家首张牌为1");
        }

        if(event.getMessage().contentToString().equals("/adminassurancewin")){
            if(isGroupMessage(event)){
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(1,10);
            } else {
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(1,10);
            }
            event.getSubject().sendMessage("测试工具：已设置庄家第二张牌张牌为10");
        }

        if(event.getMessage().contentToString().equals("/adminpair")){
            if(isGroupMessage(event)){
                Integer firstCard = BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().get(0);
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(1,firstCard);
            } else {
                Integer firstCard = BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().get(0);
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfTheBookMaker(event)).getCards().set(1,firstCard);
            }
            event.getSubject().sendMessage("测试工具：已设置庄家前两张牌相同");
        }

        if(event.getMessage().contentToString().equals("/adminsplit")){
            if(isGroupMessage(event)){
                Integer firstCard = BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().get(0);
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().set(1,firstCard+13);
            } else {
                Integer firstCard = BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().get(0);
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().set(1,firstCard+13);
            }
            event.getSubject().sendMessage("测试工具：已设置玩家前两张牌相同");
        }

        if(event.getMessage().contentToString().equals("/admin777")){
            if(isGroupMessage(event)){
                List<Integer> cardList = BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards();
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().removeAll(cardList);
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(8);
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(6);
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(7);
            } else {
                List<Integer> cardList = BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards();
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().removeAll(cardList);
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(8);
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(6);
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).getCards().add(7);
            }
            event.getSubject().sendMessage("测试工具：已设置玩家三张牌是6 7 8");
        }

        if(event.getMessage().contentToString().equals("/admindouble")){
            List<Integer> cardList = new ArrayList<>();
            cardList.add(1);
            cardList.add(10);
            if(isGroupMessage(event)){
                BlackJack.getINSTANCE().globalGroupData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).setCards(cardList);
            } else {
                BlackJack.getINSTANCE().globalFriendData.get(BlackJack.indexInTheList(event)).getBlackJackPlayerList().get(BlackJack.indexOfThePlayer(event)).setCards(cardList);
            }
            event.getSubject().sendMessage("测试工具：已设置玩家前两张牌为10和A");
        }

    }


}
