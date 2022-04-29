package mirai.chitung.plugin.core.responder.feedback

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.MessageUtil

@ResponderAutoRegistry("意见反馈", RespondFrom.GroupAndFriend, Priority.Lowest)
object Greeting : Responder {
    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.rawText.startsWith("意见反馈")) {
            runBlocking {
                launch {
                    MessageUtil.notifyDevGroup(
                        "来自${event.body.sender.id} - ${event.body.senderName}的反馈意见：${event.rawText}",
                        event.body.bot
                    )
                    event.body.subject.sendMessage("您的意见我们已经收到。")
                }
            }
            return true
        }
        return false
    }
}