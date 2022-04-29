package mirai.chitung.plugin.core.responder.feastinghelper

import mirai.chitung.plugin.utils.readLinesFromResource
import java.util.*
import kotlin.random.Random

object FoodNameCluster {
    private val foods = "/THUOCL/THUOCL_food.txt".readLinesFromResource()!!
    private val foodsWithoutPizza = foods.filter { foodStr -> !foodStr.contains("披萨") && !foodStr.contains("比萨") }
    private val drinkIngredient = DrinkIngredient(
        listOf(
            "铁观音奶茶",
            "大红袍奶茶",
            "四季奶青",
            "柠檬养乐多",
            "芒果杨枝甘露",
            "多肉葡萄",
            "多肉莓莓",
            "芝芝莓莓",
            "草莓奶昔",
            "巧克力奶昔",
            "幽兰拿铁",
            "四季春茶",
            "乌龙奶茶",
            "抹茶鲜牛乳",
            "奶绿",
            "冻顶乌龙茶",
            "茉莉绿茶",
            "红茶玛奇朵",
            "阿华田",
            "红茶拿铁",
            "绿茶拿铁",
            "三季虫",
            "豆乳米麻薯",
            "血糯米奶茶",
            "茉莉奶绿",
            "桂花酒酿奶茶",
            "黄金椰椰奶茶",
            "双皮奶奶茶",
            "西瓜啵啵",
            "桂花龙眼冰",
            "奥利奥蛋糕奶茶",
            "杨枝甘露",
            "百香凤梨",
            "手捣芒果绿",
            "老红糖奶茶",
            "海盐抹茶芝士",
            "阿华田",
            "人间烟火",
            "芊芊马卡龙",
            "桂花弄",
            "筝筝纸鸳",
            "烟火易冷",
            "素颜锡兰",
            "不知冬",
            "声声乌龙",
            "凤栖绿桂",
            "栀晓",
            "草莓桃桃",
            "硫酸铜溶液"
        ),
        listOf("加珍珠", "加波霸", "加布丁", "加龟苓", "加脆波波", "加寒天", "加椰果", "加红豆", "加三兄弟", "加咖啡冻", "加冰激凌"),
        listOf("全糖", "半糖", "三分糖", "无糖", "七分糖", "少糖", "少少糖", "少少少糖", "不额外加糖")
    )

    fun pick(type: FoodType, id: Long = 0): String {
        return when (type) {
            FoodType.Drink -> pickDrink(id)
            FoodType.Common -> pickFood()
            FoodType.Pizza -> pickPizza()
        }
    }

    private fun pickFood(): String {
        val flag = Random.nextInt(2)
        val start = when (flag) {
            0 -> "今天要不要尝尝 "
            else -> "要不要来点 "
        }
        val end = when (flag) {
            0 -> "？"
            else -> " 吃吃？"
        }
        val picked: MutableList<String> = mutableListOf()
        var i = 0
        while (i < 3) {
            val food = foods[Random.nextInt(foods.size)]
            if (picked.contains(food)) {
                i--
            } else {
                picked.add(food)
            }
            i++
        }
        return "$start${picked.joinToString("，")}$end"
    }

    private fun pickPizza(): String {
        val ingredientSum = Random.nextInt(8) + 3
        val ingredients: MutableList<String> = ArrayList()
        var i = 0
        while (i < ingredientSum) {
            val ingredient = foodsWithoutPizza[Random.nextInt(foodsWithoutPizza.size)]
            if (ingredients.contains(ingredient)) {
                i--
            } else {
                ingredients.add(ingredient)
            }
            i++
        }
        return "您点的 ${ingredients.joinToString("")}披萨 做好了"
    }

    private fun pickDrink(id: Long): String {
        return "您的饮品是${mixDrink(pickPersonalizedHourlyIngredients(id))}"
    }

    private fun pickPersonalizedHourlyIngredients(id: Long): IntArray {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val date = calendar[Calendar.DATE]
        val hour = calendar[Calendar.HOUR_OF_DAY] //获得当前时间
        val fullDate = year + month * 10000 + date * 1000000 + hour * 100000000L //用时间和小时构成一个10位数
        val getSixNum = fullDate * 1000000L / id % 1000000L //除以QQ号之后获得这个数的最后六位
        val firstTwoNum = getSixNum / 10000
        val middleTwoNum = getSixNum % 10000 / 100
        val lastTwoNum = getSixNum % 100 //获得这个数的三组两位数；
        val randomTea = IntArray(3) //定义返回数组
        randomTea[0] = Math.toIntExact(firstTwoNum % drinkIngredient.base.size)
        randomTea[1] = Math.toIntExact(middleTwoNum % drinkIngredient.topping.size)
        randomTea[2] = Math.toIntExact(lastTwoNum % drinkIngredient.sugarLevel.size)
        return randomTea
    }

    private fun mixDrink(randomTea: IntArray): String {
        return "${drinkIngredient.base[randomTea[0]]}${drinkIngredient.topping[randomTea[1]]}${drinkIngredient.sugarLevel[randomTea[2]]}"
    }

}

internal data class DrinkIngredient(val base: List<String>, val topping: List<String>, val sugarLevel: List<String>)

enum class FoodType {
    Common,
    Pizza,
    Drink
}