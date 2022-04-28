package mirai.chitung.plugin.core.responder

import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content

data class PreprocessedMessageEvent(val event: MessageEvent) {
    val typeBitMask: Byte = when (event) {
        is GroupMessageEvent -> 1
        is FriendMessageEvent -> 2
        else -> 0
    }
    val rawText = event.message.content
}