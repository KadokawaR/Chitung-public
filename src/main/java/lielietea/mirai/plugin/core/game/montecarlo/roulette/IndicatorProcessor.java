package lielietea.mirai.plugin.core.game.montecarlo.roulette;

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IndicatorProcessor {

    enum Indicator{
        Black("黑色注",2),
        Red("红色注",2),
        Odd("单数注",2),
        Even("双数注",2),
        Line("行注",12),
        Column("直列注",3),
        Four("四角区域注",9),
        Number("单注",36),
        Six("双行注",6),
        Part("1/3区域注",3),
        Half("1/2区域注",2),

        Wrong("错误下注",0);

        private final String name;
        private final int time;
        Indicator(String name, int time) { this.name = name; this.time = time; }

        public String getName() { return this.name; }
        public int getTime() { return this.time; }
    }

    enum Status{
        Cool,
        WrongNumber
    }

    static final Set<String> IndicatorString = ImmutableSet.of(
            "红","黑","红色","黑色","B","R",
            "奇","单","O","偶","双","E","单数","双数","奇数","偶数",
            "N","L","C","F","S","H","P",
            "RED","ODD",
            "EVEN",
            "BLACK"
    );

    static boolean hasIndicator(String content){
        boolean result = false;
        for (String s : IndicatorString) {
            result = result || content.contains(s);
        }
        return result;
    }

    static Indicator stringToIndicator(String content){
        switch(content) {
            case "红":
            case "红色":
            case "RED":
            case "R":
                return Indicator.Red;
            case "黑":
            case "黑色":
            case "BLACK":
            case "B":
                return Indicator.Black;
            case "单数":
            case "单":
            case "奇数":
            case "奇":
            case "ODD":
            case "O":
                return Indicator.Odd;
            case "双数":
            case "双":
            case "偶数":
            case "偶":
            case "EVEN":
            case "E":
                return Indicator.Even;
            case "N":
                return Indicator.Number;
            case "L":
                return Indicator.Line;
            case "P":
                return Indicator.Part;
            case "H":
                return Indicator.Half;
            case "C":
                return Indicator.Column;
            case "F":
                return Indicator.Four;
            case "S":
                return Indicator.Six;
        }
        return Indicator.Wrong;
    }

    static List<RouletteBet> processString(String content){
        content = content.toUpperCase();
        String[] splitContent = content.split(" ");
        List<RouletteBet> betList = new ArrayList<>();

        for(String temp:splitContent){

            //不存在任何indicator
            if(!hasIndicator(temp)){
                betList.add(new RouletteBet(Indicator.Wrong,null,Status.Cool));
                continue;
            }

            //转换错误
            if(stringToIndicator(temp.replaceAll("[0-9]", "")) == Indicator.Wrong){
                betList.add(new RouletteBet(Indicator.Wrong,null,Status.Cool));
                continue;
            }

            Indicator tempIndicator = stringToIndicator(temp.replaceAll("[0-9]", ""));

            //不需要位置的indicator
            if(doesntNeedNumber(tempIndicator)){
                betList.add(new RouletteBet(tempIndicator,null,Status.Cool));
                continue;
            }

            String tempWithoutLetter = temp.replaceAll("[^0-9]", "");
            Integer location;
            if(tempWithoutLetter.equals("")) location=null;
            else location = Integer.parseInt(tempWithoutLetter);

            Status tempStatus;
            if(isCorrectNumber(tempIndicator,location)){ tempStatus = Status.Cool; } else tempStatus = Status.WrongNumber;

            betList.add(new RouletteBet(tempIndicator,location,tempStatus));
        }

        return betList;
    }

    //是不是红黑单双四种不需要数字指示的指示器
    static boolean doesntNeedNumber(Indicator indicator){
        return indicator==Indicator.Red||indicator==Indicator.Black||indicator==Indicator.Odd||indicator==Indicator.Even;
    }

    //查看范围
    static boolean isCorrectNumber(Indicator indicator, Integer location){
        if (location==null) return false;
        if (location>36||location<0) return false;

        switch(indicator){
            case Half:
            case Part:
            case Line:
            case Column:
                return (location!=0);
            case Number:
                return true;
            case Four:
                return (location<33&&location!=0&&location%3!=0);
            case Six:
                return (location!=0&&location%3==1&&location!=34);
        }
        return false;
    }
}
