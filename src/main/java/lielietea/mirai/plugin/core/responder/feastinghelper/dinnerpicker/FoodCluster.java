package lielietea.mirai.plugin.core.responder.feastinghelper.dinnerpicker;

import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FoodCluster {
    final List<String> foods;
    final List<String> foodsWithoutPizza;

    static final String DEFAULT_FOOD_TEXT = "/THUOCL/THUOCL_food.txt";
    static final Random rand = new Random();

    FoodCluster() {
        foods = new ArrayList<>();
        InputStream is = FoodCluster.class.getResourceAsStream(DEFAULT_FOOD_TEXT);
        assert is != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
        String str;
        for (; ; ) {
            try {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                str = str.substring(0, str.indexOf("\t"));
                foods.add(str);
            } catch (IOException e) {
                //logger.fatal("转换食品列表文件为对象失败！",e);
                break;
            }
        }
        foodsWithoutPizza = new ArrayList<>();
        for (String food : foods) {
            if (!food.contains("匹萨") && !food.contains("比萨"))
                foodsWithoutPizza.add(food);
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean reload() {
        foods.clear();
        InputStream is = FoodCluster.class.getResourceAsStream(DEFAULT_FOOD_TEXT);
        assert is != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
        String str;
        for (; ; ) {
            try {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                str = str.substring(0, str.indexOf("\t"));
                foods.add(str);
            } catch (IOException e) {
                //logger.fatal("转换食品列表文件为对象失败！",e);
                return false;
            }
        }
        foodsWithoutPizza.clear();
        for (String food : foods) {
            if (!food.contains("匹萨") && !food.contains("比萨"))
                foodsWithoutPizza.add(food);
        }
        return true;
    }

    static final FoodCluster INSTANCE = new FoodCluster();

    public static FoodCluster getINSTANCE() {
        return INSTANCE;
    }


    static MessageChain reply(MessageEvent event, Mode mode) {
        if (mode == Mode.COMMON) {
            return new At(event.getSender().getId()).plus(" " + pickFood());
        } else {
            return new At(event.getSender().getId()).plus(" " + pickPizza());
        }
    }

    //随机选三种吃的
    static String pickFood() {
        StringBuilder stringBuilder = new StringBuilder();
        int flag = rand.nextInt(2);
        if (flag == 0) {
            stringBuilder.append("今天要不要尝尝 ");
        } else {
            stringBuilder.append("要不要来点 ");
        }
        List<String> foods = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String food = getINSTANCE().foods.get(rand.nextInt(getINSTANCE().foods.size()));
            if (foods.contains(food)) {
                i--;
            } else {
                foods.add(food);
            }
        }
        for (String food : foods) {
            stringBuilder.append(food);
            if (!food.equals(foods.get(foods.size() - 1))) {
                stringBuilder.append(" ");
            }
        }
        if (flag == 0) {
            stringBuilder.append("？");
        } else {
            stringBuilder.append(" 吃吃？");
        }
        return stringBuilder.toString();
    }

    //随机添加3-10项配料
    static String pickPizza() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("您点的 ");
        int ingredientSum = rand.nextInt(8) + 3;
        List<String> ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientSum; i++) {
            String ingredient = getINSTANCE().foodsWithoutPizza.get(rand.nextInt(getINSTANCE().foodsWithoutPizza.size()));
            if (ingredients.contains(ingredient)) {
                i--;
            } else {
                ingredients.add(ingredient);
            }
        }
        for (String ingredient : ingredients) {
            stringBuilder.append(ingredient);
        }
        stringBuilder.append("披萨 做好了");
        return stringBuilder.toString();
    }

    enum Mode {
        COMMON,
        PIZZA
    }
}
