package lielietea.mirai.plugin.core.game.montecarlo.roulette;

import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

public class RouletteAreas {

    public static final Set<Integer> Black = ImmutableSet.of(
        2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35
    );

    public static final Set<Integer> Red = ImmutableSet.of(
        1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36
    );

    public static Set<Integer> getOddArea(){
        Set<Integer> res = new HashSet<>();
        for(int i=1;i<=35;i+=2){
            res.add(i);
        }
        return res;
    }

    public static Set<Integer> getEvenArea(){
        Set<Integer> res = new HashSet<>();
        for(int i=2;i<=36;i+=2){
            res.add(i);
        }
        return res;
    }

    public static Set<Integer> getLineArea(Integer location){
        Set<Integer> res = new HashSet<>();
        res.add(location);
        int remainder = location%3;
        switch(remainder){
            case 0:
                res.add(location-1);
                res.add(location-2);
                return res;
            case 1:
                res.add(location+1);
                res.add(location+2);
                return res;
            case 2:
                res.add(location-1);
                res.add(location+1);
                return res;
        }
        return res;
    }

    public static Set<Integer> getColumnArea(Integer location){
        Set<Integer> res = new HashSet<>();
        int remainder = location%3;
        if (remainder==0) remainder=3;
        for(int i=0;i<=33;i+=3){
            res.add(i+remainder);
        }
        return res;
    }

    public static Set<Integer> getFourArea(Integer location){
        Set<Integer> res = new HashSet<>();
        res.add(location);
        res.add(location+1);
        res.add(location+3);
        res.add(location+4);
        return res;
    }

    public static Set<Integer> getSixArea(Integer location){
        Set<Integer> res = new HashSet<>();
        for(int i=0;i<6;i++){
            res.add(location+i);
        }
        return res;
    }

    public static Set<Integer> getPartArea(Integer location){
        Set<Integer> res = new HashSet<>();
        int offset = ((location-1)/12)*12;
        for(int i=1;i<=12;i++){
            res.add(offset+i);
        }
        return res;
    }

    public static Set<Integer> getHalfArea(Integer location){
        Set<Integer> res = new HashSet<>();
        int offset=0;
        if(location>18) offset=18;
        for(int i=1;i<=18;i++){
            res.add(offset+i);
        }
        return res;
    }

}
