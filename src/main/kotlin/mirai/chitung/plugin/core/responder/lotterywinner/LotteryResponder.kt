package mirai.chitung.plugin.core.responder.lotterywinner

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry
import mirai.chitung.plugin.core.responder.matches

@ResponderAutoRegistry("C4")
object C4Responder : Responder {
    private val reg = Regex("(/[Cc]4)|([oO][kK] [Cc]4)")

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.matches(reg)) {
            coroutineScope {
                launch {
                    LotteryMachine.okC4(event)
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("Bummer")
object BummerResponder : Responder {
    private val reg = Regex("(/[Bb]ummer)|([oO][kK] [Bb]ummer)")

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.matches(reg)) {
            coroutineScope {
                launch {
                    LotteryMachine.okBummer(event)
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("Winner")
object WinnerResponder : Responder {
    private val reg = Regex("((/[Ww]inner)|([oO][kK] [Ww]inner))|(/(([Ll]ottery)|(乐透)|(彩票)))")

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.matches(reg)) {
            coroutineScope {
                launch {
                    LotteryMachine.okWinner(event)
                }
            }
            return true
        }
        return false
    }
}