package mirai.chitung.plugin.core.responder.fursonapunk

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.RespondFrom
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At

@ResponderAutoRegistry("兽设", RespondFrom.GroupAndFriend)
object FursonaPunkResponder : Responder {
    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.rawText == "兽设") {
            runBlocking {
                launch {
                    if (event.body is GroupMessageEvent) {
                        event.body.subject.sendMessage(
                            At(event.body.sender.id).plus(
                                FursonaCluster.createFurryFucker(
                                    event.body.sender.id
                                )
                            )
                        )
                    } else {
                        event.body.subject.sendMessage("您${FursonaCluster.createFurryFucker(event.body.sender.id)}")
                    }
                }
            }
            return true
        }
        return false
    }
}