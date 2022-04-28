package mirai.chitung.plugin.core.responder.basic

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.*

@ResponderAutoRegistry("自动回复：打招呼")
object GreetingResponder:Responder {
    private val reg = Regex("[Hh]((ello)|(i))")
    private val replies = listOf("Hi","Hello","Hey")

    override fun receive(event: PreprocessedMessageEvent): Boolean{
        if(reg.matches(event.rawText)){
            runBlocking {
                launch {
                    event.event.sender.sendMessage(replies.random())
                }
            }
            return true
        }
        return false
    }
}