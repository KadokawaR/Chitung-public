package mirai.chitung.plugin.core.responder.mahjong

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.getResourceByStream
import mirai.chitung.plugin.utils.sendTo
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import java.util.*

@ResponderAutoRegistry("掷骰子", RespondFrom.GroupAndFriend, Priority.Highest)
object FortuneTeller : Responder {
    private val luck = ArrayList(
        listOf(
            "大凶",  //一筒
            "末吉",
            "吉",
            "吉凶相半",
            "吉",
            "末吉",
            "大大吉",  //七筒
            "吉",
            "小凶後吉",  //九筒
            "吉",  //一条
            "末吉",
            "吉",
            "小凶後吉",
            "吉",
            "末吉",
            "大吉",
            "吉凶相半",
            "吉",  //九条
            "末吉",  //一万
            "半吉",
            "凶後吉",
            "半吉",
            "末吉",
            "半吉",
            "大凶",
            "半吉",
            "吉凶相半",  //九万
            "半吉",  //东
            "末吉",  //南
            "凶後大吉",  //西
            "凶後吉",  //北
            "小吉",  //中
            "小凶後吉",  //白
            "大吉",  //发
            "吉凶相半",  //春
            "小吉",
            "末吉",
            "小吉",  //冬
            "大吉",  //梅
            "中吉",
            "大吉",
            "中吉" //菊
        )
    )
    private val saying = ArrayList(
        listOf(
            "别出门了，今天注意安全。",  //一筒
            "是吉是凶并不清楚，暂定为吉！",
            "还算不错！",
            "吉凶各一半，要小心哦！",
            "其实还不错！",
            "是吉是凶并不清楚，暂定为吉！",
            "实现愿望的最高幸运，今天你会心想事成！",  //七筒
            "还不错！",
            "丢失的运气会补回来的！",  //九筒
            "还不错！",  //一条
            "是吉是凶并不清楚，暂定为吉！",
            "还可以的！",
            "丢失的运气会补回来的！",
            "还不错！",
            "是吉是凶并不清楚，暂定为吉！",
            "是仅次于大大吉的超级好运！",
            "吉凶各一半，要小心哦！",
            "还不错！",  //九条
            "是吉是凶并不清楚，暂定为吉！",  //一万
            "勉勉强强的好运！",
            "一阵不走运之后会好运的！",
            "勉勉强强的好运！",
            "是吉是凶并不清楚，暂定为吉！",
            "勉勉强强的好运！",
            "别出门了，今天注意安全。",
            "勉勉强强的好运！",
            "吉凶各一半，小心一些总不会错！",  //九万
            "勉勉强强的好运！",  //东
            "是吉是凶并不清楚，暂定为吉！",  //南
            "一阵不走运之后会行大运的！",  //西
            "一阵不走运之后会好运的！",  //北
            "微小但一定会到来的好运！",  //中
            "丢失的运气会补回来的！",  //白
            "是仅次于大大吉的超级好运！会有很好的财运！",  //发
            "吉凶各一半，要小心哦！",  //春
            "微小但一定会到来的好运！",
            "是吉是凶并不清楚，暂定为吉！",
            "微小但一定会到来的好运！",  //冬
            "是仅次于大大吉的超级好运！",  //梅
            "非常好的运气！",
            "是仅次于大大吉的超级好运！",
            "非常好的运气！姻缘不错！" //菊
        )
    )

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.contentEquals("求签") || event.contentEquals("麻将")) {
            coroutineScope {
                launch {
                    var mahjongPicPath = "/pics/mahjong/"
                    mahjongPicPath += if (Random().nextBoolean()) {
                        "Red/"
                    } else {
                        "Yellow/"
                    }
                    mahjongPicPath += getMahjong(getMahjongOfTheDay(event.body.sender.id)) + ".png"
                    if (event.isFromGroup()) {
                        MessageChainBuilder()
                            .append(At(event.body.sender.id))
                            .append("\n")
                            .append(whatDoesMahjongSay(event.body.sender.id))
                            .append("\n")
                            .append(event.body.subject.uploadImage(mahjongPicPath.getResourceByStream()!!))
                            .build().sendTo(event)
                    } else {
                        MessageChainBuilder()
                            .append(whatDoesMahjongSay(event.body.sender.id))
                            .append("\n")
                            .append(event.body.subject.uploadImage(mahjongPicPath.getResourceByStream()!!))
                            .build().sendTo(event)
                    }
                }
            }
            return true
        }
        return false
    }

    private fun getMahjongOfTheDay(id: Long): Int {
        //获取当日幸运数字
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val date = calendar[Calendar.DATE]
        val datetime = year * 1000L + month * 100 + date
        val random = Random(id + datetime)
        return random.nextInt(144)
    }

    fun getMahjong(mahjongOfTheDay: Int): String {
        val chineseNum = ArrayList(
            listOf(
                "一", "二", "三", "四", "五", "六", "七", "八", "九"
            )
        )
        val fengXiang = ArrayList(
            listOf(
                "東", "南", "西", "北"
            )
        )
        val zhongFaBai = ArrayList(
            listOf(
                "红中", "發财", "白板"
            )
        )
        val huaPai = ArrayList(
            listOf(
                "春", "夏", "秋", "冬", "梅", "兰", "竹", "菊"
            )
        )
        val mahjongNumero: Int
        return if (mahjongOfTheDay < 36) {
            mahjongNumero = mahjongOfTheDay % 9
            chineseNum[mahjongNumero] + "筒"
        } else if (mahjongOfTheDay < 72) {
            mahjongNumero = mahjongOfTheDay % 9
            chineseNum[mahjongNumero] + "条"
        } else if (mahjongOfTheDay < 108) {
            mahjongNumero = mahjongOfTheDay % 9
            chineseNum[mahjongNumero] + "萬"
        } else if (mahjongOfTheDay < 124) {
            mahjongNumero = mahjongOfTheDay % 4
            fengXiang[mahjongNumero] + "风"
        } else if (mahjongOfTheDay < 136) {
            mahjongNumero = mahjongOfTheDay % 3
            zhongFaBai[mahjongNumero]
        } else {
            mahjongNumero = mahjongOfTheDay - 136
            "花牌（" + huaPai[mahjongNumero] + "）"
        }
    }

    private fun whatDoesMahjongSay(id: Long): String {
        val mahjongOfTheDay = getMahjongOfTheDay(id)
        val mahjongNumero: Int = if (mahjongOfTheDay < 36) {
            mahjongOfTheDay % 9
        } else if (mahjongOfTheDay < 72) {
            (mahjongOfTheDay - 36) % 9 + 9
        } else if (mahjongOfTheDay < 108) {
            (mahjongOfTheDay - 72) % 9 + 18
        } else if (mahjongOfTheDay < 124) {
            (mahjongOfTheDay - 108) % 4 + 27
        } else if (mahjongOfTheDay < 136) {
            (mahjongOfTheDay - 124) % 3 + 31
        } else {
            Math.toIntExact(mahjongOfTheDay.toLong()) - 102
        }
        return """
             今天的占卜麻将牌是: ${getMahjong(mahjongOfTheDay)}
             运势是: ${luck[mahjongNumero]}
             ${saying[mahjongNumero]}
             """.trimIndent()
    }
}