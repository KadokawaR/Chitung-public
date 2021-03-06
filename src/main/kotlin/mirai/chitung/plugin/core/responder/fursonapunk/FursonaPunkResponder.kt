package mirai.chitung.plugin.core.responder.fursonapunk

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.sendTo
import net.mamoe.mirai.message.data.At

@ResponderAutoRegistry("兽设", RespondFrom.GroupAndFriend)
object FursonaPunkResponder : Responder {
    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.contentEquals("兽设")) {
            coroutineScope {
                launch {
                    if (event.isFromGroup()) {
                        At(event.body.sender.id).plus(FursonaCluster.createFurryFucker(event.body.sender.id))
                            .sendTo(event)
                    } else {
                        "您${FursonaCluster.createFurryFucker(event.body.sender.id)}".sendTo(event)
                    }
                }
            }
            return true
        }
        return false
    }
}