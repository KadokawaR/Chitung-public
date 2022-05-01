package mirai.chitung.plugin.core.responder.feedback

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.MessageUtil
import mirai.chitung.plugin.utils.sendTo

@ResponderAutoRegistry("意见反馈", RespondFrom.GroupAndFriend, Priority.Lowest)
object Greeting : Responder {
    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.contentStartsWith("意见反馈")) {
            coroutineScope {
                launch {
                    MessageUtil.notifyDevGroup(
                        "来自${event.body.sender.id} - ${event.body.senderName}的反馈意见：${event.rawText}",
                        event.body.bot
                    )
                    "您的意见我们已经收到。".sendTo(event)
                }
            }
            return true
        }
        return false
    }
}