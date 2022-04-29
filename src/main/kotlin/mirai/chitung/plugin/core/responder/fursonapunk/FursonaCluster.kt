package mirai.chitung.plugin.core.responder.fursonapunk

import java.util.*
import kotlin.random.Random

object FursonaCluster {
    private val fursonaData = FursonaData()

    //先是全身和上下分装的判定，(全身size) : (上半身size+下半身size)。
    //然后上半身size有30%的概率是赤裸上身，下半身有40%没穿任何东西。然后再在词库里随机抽取。
    //全身有4%的概率什么都不穿，就算一开始不是轮到全身size也是有可能什么都不穿的（上下半身分开的6%）。

    //30%概率什么都不戴

    //先是全身和上下分装的判定，(全身size) : (上半身size+下半身size)。
    //然后上半身size有30%的概率是赤裸上身，下半身有40%没穿任何东西。然后再在词库里随机抽取。
    //全身有4%的概率什么都不穿，就算一开始不是轮到全身size也是有可能什么都不穿的（上下半身分开的6%）。
    //30%概率什么都不戴
    private fun getRandomHats(random: Random): String {
        val isWearingHats = random.nextInt(10) < 7
        val isHats =
            random.nextInt(fursonaData.Hats.size + fursonaData.Bags.size) < fursonaData.Hats.size
        return if (isWearingHats) {
            if (isHats) {
                "戴着" + fursonaData.Adjective[random.nextInt(fursonaData.Adjective.size)] + "的" + fursonaData.Hats[random.nextInt(
                    fursonaData.Hats.size
                )] + "，"
            } else {
                "背着" + fursonaData.Adjective[random.nextInt(fursonaData.Adjective.size)] + "的" + fursonaData.Bags[random.nextInt(
                    fursonaData.Bags.size
                )] + "，"
            }
        } else {
            ""
        }
    }

    private fun getRandomClothes(random: Random): String {
        val isNaked = random.nextInt(25) < 1
        val topNaked = random.nextInt(10) < 3
        val bottomNaked = random.nextInt(10) < 4
        val isSuits =
            random.nextInt(fursonaData.Suits.size + fursonaData.Tops.size + fursonaData.Bottoms.size) < fursonaData.Suits.size
        var randomClothes = ""
        if (isNaked || topNaked && bottomNaked) {
            return "全身一丝不挂的，"
        }
        if (isSuits) {
            randomClothes =
                "身穿" + fursonaData.Adjective[random.nextInt(fursonaData.Adjective.size)] + "的" + fursonaData.Color[random.nextInt(
                    fursonaData.Color.size
                )] + fursonaData.Suits[random.nextInt(fursonaData.Suits.size)] + "，"
        } else {
            randomClothes = if (topNaked) {
                "赤裸上身，"
            } else {
                "身穿" + fursonaData.Adjective[random.nextInt(fursonaData.Adjective.size)] + "的" + fursonaData.Color[random.nextInt(
                    fursonaData.Color.size
                )] + fursonaData.Tops[random.nextInt(fursonaData.Tops.size)] + "，"
            }
            randomClothes = if (bottomNaked) {
                randomClothes + "下半身一丝不挂，"
            } else {
                randomClothes + "腿穿" + fursonaData.Adjective[random.nextInt(fursonaData.Adjective.size)] + "的" + fursonaData.Color[random.nextInt(
                    fursonaData.Color.size
                )] + fursonaData.Bottoms[random.nextInt(fursonaData.Bottoms.size)] + "，"
            }
        }
        return randomClothes
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
    private fun getSpecies(random: Random): String {
        val speciesRandom = random.nextInt(100)
        return if (speciesRandom < 13) {
            fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "犬"
        } else if (speciesRandom < 26) {
            val wolfRandom = random.nextInt(100)
            if (wolfRandom < 30) {
                "黑狼"
            } else if (wolfRandom < 55) {
                "灰狼"
            } else if (wolfRandom < 75) {
                "白狼"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "狼"
            }
        } else if (speciesRandom < 39) {
            val bearRandom = random.nextInt(100)
            if (bearRandom < 35) {
                "白熊"
            } else if (bearRandom < 60) {
                "棕熊"
            } else if (bearRandom < 75) {
                "黑熊"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "熊"
            }
        } else if (speciesRandom < 52) {
            val lionRandom = random.nextInt(10)
            if (lionRandom < 2) {
                "狮"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "狮"
            }
        } else if (speciesRandom < 65) {
            val tigerRandom = random.nextInt(10)
            if (tigerRandom < 7) {
                "虎"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "虎"
            }
        } else if (speciesRandom < 72) {
            val jeopardRandom = random.nextInt(10)
            if (jeopardRandom < 4) {
                "豹"
            } else if (jeopardRandom < 8) {
                "黑豹"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "豹"
            }
        } else if (speciesRandom < 79) {
            fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "龙"
        } else if (speciesRandom < 86) {
            val pandaRandom = random.nextInt(10)
            if (pandaRandom < 8) {
                "熊猫"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "熊猫"
            }
        } else if (speciesRandom < 89) {
            val foxRandom = random.nextInt(10)
            if (foxRandom < 2) {
                "白狐"
            } else if (foxRandom < 6) {
                "狐狸"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "狐狸"
            }
        } else if (speciesRandom < 92) {
            val catRandom = random.nextInt(10)
            if (catRandom < 3) {
                "黑猫"
            } else if (catRandom < 6) {
                "花猫"
            } else {
                fursonaData.Color[random.nextInt(fursonaData.Color.size)] + "猫"
            }
        } else {
            fursonaData.Color[random.nextInt(fursonaData.Color.size)] + fursonaData.Species[random.nextInt(
                fursonaData.Species.size
            )]
        }
    }
    //@用户 的兽设是: 在[蒸汽朋克下]的[必胜客]，[冥王星人建立了戴森球]。
    //因为[孟姜女哭塌了长城]，[计划着去殖民英仙座]的，戴着[黑色][针织帽]，
    //身穿[ADJ1][ADJ2非常暴露]的[灰色][西装大衣]，手握[茶颜悦色]的[彩虹色][狐狸]兽人。

    //@用户 的兽设是: 在[蒸汽朋克下]的[必胜客]，[冥王星人建立了戴森球]。
    //因为[孟姜女哭塌了长城]，[计划着去殖民英仙座]的，戴着[黑色][针织帽]，
    //身穿[ADJ1][ADJ2非常暴露]的[灰色][西装大衣]，手握[茶颜悦色]的[彩虹色][狐狸]兽人。
    fun createFurryFucker(id: Long): String {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val date = calendar[Calendar.DATE]
        val datetime = year * 1000L + month * 100 + date
        val random = Random(id + datetime)
        val era = fursonaData.Era[random.nextInt(fursonaData.Era.size)]
        val location =
            fursonaData.Location[random.nextInt(fursonaData.Location.size)]
        val random1 = random.nextInt(fursonaData.Reason.size)
        var random2 = random1
        while (random1 == random2) {
            random2 = random.nextInt(fursonaData.Reason.size)
        }
        val reason1 = fursonaData.Reason[random1]
        val reason2 = fursonaData.Reason[random2]
        val action = fursonaData.Action[random.nextInt(fursonaData.Action.size)]
        val items = fursonaData.Items[random.nextInt(fursonaData.Items.size)]
        return "的今日兽设是：在" + era + "的" + location + "，" + reason1 + "。因为" + reason2 + '，' + action + "的，" + getRandomHats(
            random
        ) + getRandomClothes(random) + "手握" + items + "的" + getSpecies(random) + "兽人。"
    }
}


