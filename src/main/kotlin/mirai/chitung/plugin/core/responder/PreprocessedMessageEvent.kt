package mirai.chitung.plugin.core.responder

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.content

data class PreprocessedMessageEvent(val body: MessageEvent) {
    val typeBitMask: Byte = when (body) {
        is GroupMessageEvent -> 1
        is FriendMessageEvent -> 2
        else -> 0
    }
    val rawText = body.message.content
}

fun PreprocessedMessageEvent.matches(reg: Regex): Boolean = reg.matches(this.rawText)

fun PreprocessedMessageEvent.contentHas(content: String): Boolean = this.rawText.contains(content)

fun PreprocessedMessageEvent.contentEquals(content: String): Boolean = this.rawText == content

fun PreprocessedMessageEvent.contentStartsWith(content: String): Boolean = this.rawText.startsWith(content)

fun PreprocessedMessageEvent.isFromGroup(): Boolean = this.body is GroupMessageEvent

fun PreprocessedMessageEvent.createAt(): At = At(this.body.sender)

fun PreprocessedMessageEvent.group(): Group? = if (this.isFromGroup()) this.body.subject as Group else null

fun PreprocessedMessageEvent.sender(): User = this.body.sender

fun PreprocessedMessageEvent.member(): Member? = if (this.isFromGroup()) this.body.sender as Member else null