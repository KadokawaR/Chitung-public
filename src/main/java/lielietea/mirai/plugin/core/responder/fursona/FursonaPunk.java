package lielietea.mirai.plugin.core.responder.fursona;

import com.google.gson.Gson;
import lielietea.mirai.plugin.core.responder.RespondTask;
import lielietea.mirai.plugin.core.responder.MessageResponder;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FursonaPunk implements MessageResponder<MessageEvent> {
    static final List<MessageResponder.MessageType> TYPES = new ArrayList<>(Collections.singletonList(MessageResponder.MessageType.GROUP));
    static final Fursona FURSONA_COMPONENTS;
    static final String FURSONA_PATH = "/cluster/fursona.json";

    static {
        InputStream is = FursonaPunk.class.getResourceAsStream(FURSONA_PATH);
        assert is != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        Gson gson = new Gson();
        FURSONA_COMPONENTS = gson.fromJson(br, Fursona.class);
    }

    @Override
    public boolean match(MessageEvent event) {
        return event.getMessage().contentToString().equals("兽设");
    }

    @Override
    public RespondTask handle(MessageEvent event) {
        return RespondTask.of(event, new At(event.getSender().getId()).plus(createFurryFucker(FURSONA_COMPONENTS, event)), this);
    }

    @NotNull
    @Override
    public List<MessageType> types() {
        return TYPES;
    }

    //先是全身和上下分装的判定，(全身size) : (上半身size+下半身size)。
    //然后上半身size有30%的概率是赤裸上身，下半身有40%没穿任何东西。然后再在词库里随机抽取。
    //全身有4%的概率什么都不穿，就算一开始不是轮到全身size也是有可能什么都不穿的（上下半身分开的6%）。

    //30%概率什么都不戴

    static String getRandomHats(Fursona furfur, Random random) {
        boolean isWearingHats = (random.nextInt(10) < 7);
        boolean isHats = (random.nextInt(furfur.Hats.length + furfur.Bags.length) < furfur.Hats.length);
        if (isWearingHats) {
            if (isHats) {
                return "戴着" + furfur.Adjective1[random.nextInt(furfur.Adjective1.length)] + "的" + furfur.Hats[random.nextInt(furfur.Hats.length)] + "，";
            } else {
                return "背着" + furfur.Adjective1[random.nextInt(furfur.Adjective1.length)] + "的" + furfur.Bags[random.nextInt(furfur.Bags.length)] + "，";
            }
        } else {
            return "";
        }
    }

    static String getRandomClothes(Fursona furfur, Random random) {
        boolean isNaked = (random.nextInt(25) < 1);
        boolean topNaked = (random.nextInt(10) < 3);
        boolean bottomNaked = (random.nextInt(10) < 4);
        boolean isSuits = (random.nextInt(furfur.Suits.length + furfur.Tops.length + furfur.Bottoms.length) < furfur.Suits.length);
        String randomClothes = "";
        if ((isNaked) || (topNaked && bottomNaked)) {
            return "全身一丝不挂的，";
        }
        if (isSuits) {
            randomClothes = "身穿" + furfur.Adjective1[random.nextInt(furfur.Adjective1.length)] + "的" + furfur.Color[random.nextInt(furfur.Color.length)] + furfur.Suits[random.nextInt(furfur.Suits.length)] + "，";
        } else {
            if (topNaked) {
                randomClothes = "赤裸上身，";
            } else {
                randomClothes = "身穿" + furfur.Adjective1[random.nextInt(furfur.Adjective1.length)] + "的" + furfur.Color[random.nextInt(furfur.Color.length)] + furfur.Tops[random.nextInt(furfur.Tops.length)] + "，";
            }
            if (bottomNaked) {
                randomClothes = randomClothes + "下半身一丝不挂，";
            } else {
                randomClothes = randomClothes + "腿穿" + furfur.Adjective1[random.nextInt(furfur.Adjective1.length)] + "的" + furfur.Color[random.nextInt(furfur.Color.length)] + furfur.Bottoms[random.nextInt(furfur.Bottoms.length)] + "，";
            }
        }
        return randomClothes;
    }

    //有一些颜色应该权重格外高？比如黑色和白色
    //对于狼：控制在30%的黑、25%的灰和20%的白
    //对于熊：控制在35%的白、25%的棕和15%的黑
    //对于虎：70%不放颜色
    //对于狮：80%不放颜色
    //对于熊猫：80%不放颜色
    //对于豹：40%的黑色，40%不放颜色，
    //对于狐狸：20%白，40%没有颜色
    //对于猫：30%黑,30%花

    //犬,狼,熊,虎,狮,豹,龙,熊猫,狐狸,猫,鲨鱼,牛,鹰,兔,鼠,水獭,非地球物种,[数据删除]
    //13,13,13,13,13,7,7,7,3,3,(8)分配给剩下的物种
    //json里面存储的是剩下的物种

    static String getSpecies(Fursona furfur, Random random) {
        int speciesRandom = random.nextInt(100);
        String species = "";
        if (speciesRandom < 13) {
            return furfur.Color[random.nextInt(furfur.Color.length)] + "犬";
        } else if (speciesRandom < 26) {
            int wolfRandom = random.nextInt(100);
            if (wolfRandom < 30) {
                return "黑狼";
            } else if (wolfRandom < 55) {
                return "灰狼";
            } else if (wolfRandom < 75) {
                return "白狼";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "狼";
            }
        } else if (speciesRandom < 39) {
            int bearRandom = random.nextInt(100);
            if (bearRandom < 35) {
                return "白熊";
            } else if (bearRandom < 60) {
                return "棕熊";
            } else if (bearRandom < 75) {
                return "黑熊";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "熊";
            }
        } else if (speciesRandom < 52) {
            int lionRandom = random.nextInt(10);
            if (lionRandom < 2) {
                return "狮";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "狮";
            }
        } else if (speciesRandom < 65) {
            int tigerRandom = random.nextInt(10);
            if (tigerRandom < 7) {
                return "虎";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "虎";
            }
        } else if (speciesRandom < 72) {
            int jeopardRandom = random.nextInt(10);
            if (jeopardRandom < 4) {
                return "豹";
            } else if (jeopardRandom < 8) {
                return "黑豹";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "豹";
            }

        } else if (speciesRandom < 79) {
            return furfur.Color[random.nextInt(furfur.Color.length)] + "龙";
        } else if (speciesRandom < 86) {
            int pandaRandom = random.nextInt(10);
            if (pandaRandom < 8) {
                return "熊猫";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "熊猫";
            }
        } else if (speciesRandom < 89) {
            int foxRandom = random.nextInt(10);
            if (foxRandom < 2) {
                return "白狐";
            } else if (foxRandom < 6) {
                return "狐狸";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "狐狸";
            }
        } else if (speciesRandom < 92) {
            int catRandom = random.nextInt(10);
            if (catRandom < 3) {
                return "黑猫";
            } else if (catRandom < 6) {
                return "花猫";
            } else {
                return furfur.Color[random.nextInt(furfur.Color.length)] + "猫";
            }
        } else {
            return furfur.Color[random.nextInt(furfur.Color.length)] + furfur.Species[random.nextInt(furfur.Species.length)];
        }
    }
    //@用户 的兽设是: 在[蒸汽朋克下]的[必胜客]，[冥王星人建立了戴森球]。
    //因为[孟姜女哭塌了长城]，[计划着去殖民英仙座]的，戴着[黑色][针织帽]，
    //身穿[ADJ1][ADJ2非常暴露]的[灰色][西装大衣]，手握[茶颜悦色]的[彩虹色][狐狸]兽人。

    static String createFurryFucker(Fursona furfur, MessageEvent event) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        long datetime = year * 1000L + month * 100 + date;
        Random random = new Random(event.getSender().getId() + datetime);
        String era = furfur.Era[random.nextInt(furfur.Era.length)];
        String location = furfur.Location[random.nextInt(furfur.Location.length)];
        int random1 = random.nextInt(furfur.Reason.length);
        int random2 = random1;
        while (random1 == random2) {
            random2 = random.nextInt(furfur.Reason.length);
        }
        String reason1 = furfur.Reason[random1];
        String reason2 = furfur.Reason[random2];
        String action = furfur.Action[random.nextInt(furfur.Action.length)];
        String items = furfur.Items[random.nextInt(furfur.Items.length)];
        return "的兽设是：在" + era + "的" + location + "，" + reason1 + "。因为" + reason2 + '，' + action + "的，" + getRandomHats(furfur, random) + getRandomClothes(furfur, random) + "手握" + items + "的" + getSpecies(furfur, random) + "兽人。";
    }

    @Override
    public String getName() {
        return "兽设";
    }

    enum wordType {
        Species, Era, Location, Reason, Action, Color, Adjective1, Adjective2, Tops, Bottoms, Suits, Hats, Bags, Items
    }
}
