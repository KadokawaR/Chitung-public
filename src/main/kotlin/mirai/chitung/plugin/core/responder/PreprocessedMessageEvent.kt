package mirai.chitung.plugin.core.responder

import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

data class PreprocessedMessageEvent(val body: MessageEvent) {
    val typeBitMask: Byte = when (body) {
        is GroupMessageEvent -> 1
        is FriendMessageEvent -> 2
        else -> 0
    }
    val rawText = body.message.content
}

fun PreprocessedMessageEvent.matches(reg: Regex):Boolean = reg.matches(this.rawText)

fun PreprocessedMessageEvent.contentHas(content: String):Boolean = this.rawText.contains(content)

fun PreprocessedMessageEvent.contentEquals(content: String):Boolean = this.rawText == content

fun PreprocessedMessageEvent.contentStartsWith(content: String):Boolean = this.rawText.startsWith(content)

fun PreprocessedMessageEvent.isFromGroup():Boolean = this.body is GroupMessageEvent