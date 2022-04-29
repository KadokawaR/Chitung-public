package mirai.chitung.plugin.core.responder.feastinghelper

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.RespondFrom
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry

@ResponderAutoRegistry("吃什么", RespondFrom.GroupAndFriend)
object MealPicker : Responder {
    private val reg =
        Regex("(/[Mm]eal)|(/[Dd]inner)|(/吃啥)|([oO][kK] [Mm]eal)|([oO][kK] [Dd]inner)|(((早饭)|(午饭)|(晚饭)|(夜宵)|(今天)|(今晚)|(早茶)|(宵夜))吃什么)")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(FoodNameCluster.pick(FoodType.Common))
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("披萨", RespondFrom.GroupAndFriend)
object PizzaPicker : Responder {
    private val reg = Regex("(/[Pp]izza)|([oO][kK] [Pp]izza)")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(FoodNameCluster.pick(FoodType.Pizza))
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("奶茶", RespondFrom.GroupAndFriend)
object DrinkPicker : Responder {
    private val reg = Regex(".*((喝点什么)|(奶茶)|(喝了什么)|(喝什么)|(有点渴)|(好渴)|(来一杯)).*")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(FoodNameCluster.pick(FoodType.Drink, event.body.sender.id))
                }
            }
            return true
        }
        return false
    }
}