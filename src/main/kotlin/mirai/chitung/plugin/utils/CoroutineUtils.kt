package mirai.chitung.plugin.utils

import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

suspend fun String.sendTo(contact: Contact) = contact.sendMessage(this)

suspend fun String.sendTo(event: MessageEvent) = event.subject.sendMessage(this)

suspend fun String.sendTo(event: PreprocessedMessageEvent) = event.body.subject.sendMessage(this)

suspend fun MessageChain.sendTo(contact: Contact) = contact.sendMessage(this)

suspend fun MessageChain.sendTo(event: MessageEvent) = event.subject.sendMessage(this)

suspend fun MessageChain.sendTo(event: PreprocessedMessageEvent) = event.body.subject.sendMessage(this)