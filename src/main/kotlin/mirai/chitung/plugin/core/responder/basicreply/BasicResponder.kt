package mirai.chitung.plugin.core.responder.basicreply

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.RespondFrom
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry

@ResponderAutoRegistry("自动回复：打招呼", RespondFrom.GroupAndFriend)
object Greeting : Responder {
    private val reg = Regex("[Hh]((ello)|(i))")
    private val replies = listOf("Hi", "Hello", "Hey")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(replies.random())
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("自动回复：告别", RespondFrom.GroupAndFriend)
object Goodbye : Responder {
    private val reg = Regex(".*((下线了)|(我走了)|(拜拜)).*")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(BasicReplyCluster.reply(BasicReplyType.GoodBye))
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("自动回复：反脏话")
object AntiDirtyWord : Responder {
    private val reg =
        Regex(".*((([日干操艹草滚槽曹糙])([你尼泥腻妮])([妈马麻码吗玛]))|(([Mm])otherfucker)|(([Ff])uck ([Yy])ou)|(([Ff])(Uu)(Cc)(Kk))|(野爹)).*")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(BasicReplyCluster.reply(BasicReplyType.AntiDirtyWord))
                }
            }
            return true
        }
        return false
    }
}

@ResponderAutoRegistry("自动回复：反守望先锋")
object AntiOverWatch : Responder {
    private val reg = Regex(".*((([Oo])ver[Ww]atch)|(守望((先锋)|(屁股)))|(([玩打])((OW)|(ow)))).*")

    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(BasicReplyCluster.reply(BasicReplyType.AntiOverWatch))
                }
            }
            return true
        }
        return false
    }
}
