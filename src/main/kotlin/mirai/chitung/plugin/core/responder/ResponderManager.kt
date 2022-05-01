package mirai.chitung.plugin.core.responder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.harbor.Harbor.count
import mirai.chitung.plugin.core.harbor.Harbor.isReachingPortLimit
import mirai.chitung.plugin.core.harbor.PortRequestInfos
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import kotlin.experimental.and
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor


object ResponderManager {
    private var list: MutableList<BoxedResponder> = ArrayList()

    @Synchronized
    fun MessageEvent.sendToResponderManager() {
        if (this.isQualifiedForResponder()) {
            CoroutineScope(Dispatchers.Default).launch {
                PreprocessedMessageEvent(this@sendToResponderManager).handle()
            }
        }
    }

    private fun MessageEvent.isQualifiedForResponder(): Boolean {
        //TODO 加入筛去卡片消息的内容
        return when {
            reachLimit(this) -> false
            this is GroupMessageEvent -> true
            this is FriendMessageEvent -> true
            else -> false
        }
    }

    private fun reachLimit(event: MessageEvent): Boolean {
        return when {
            isReachingPortLimit(PortRequestInfos.TOTAL_DAILY, 0L) -> true
            event is GroupMessageEvent && isReachingPortLimit(PortRequestInfos.GROUP_MINUTE, event.subject.id) -> true
            isReachingPortLimit(PortRequestInfos.PERSONAL, event.subject.id) -> true
            else -> false
        }
    }

    private suspend fun PreprocessedMessageEvent.handle() {
        for (r in list) {
            if (r.info.from.typeBit.and(this.typeBitMask) > 0) {
                if (r.responder.receive(this)) {
                    this.acknowledgeHarbor()
                    break
                }
            }
        }
    }

    private fun PreprocessedMessageEvent.acknowledgeHarbor() {
        if (this.body.subject is Group)
            count(PortRequestInfos.GROUP_MINUTE, this.body.subject.id)
        count(PortRequestInfos.PERSONAL, this.body.sender.id)
        count(PortRequestInfos.TOTAL_DAILY, 0L)
    }

    fun setup() {
        val reflection = Reflections(
            ConfigurationBuilder()
                .forPackage("mirai.chitung")
                .setScanners(Scanners.TypesAnnotated)
        )
        val classes = reflection.getTypesAnnotatedWith(ResponderAutoRegistry::class.java).map { clazz -> clazz.kotlin }
        for (c in classes) {
            if (!c.isSubclassOf(Responder::class)) {
                println("[Error]: Responder $c 不是一个 Responder! 注册失败!")
            }
            if (c.primaryConstructor != null) {
                if (c.primaryConstructor!!.parameters.isNotEmpty()) {
                    println("[Error]: Responder $c 的主构建方法方法! 注册失败!")
                } else {
                    list.add(
                        BoxedResponder(
                            c.primaryConstructor!!.call() as Responder,
                            c.annotations.find { annotation -> annotation is ResponderAutoRegistry }!! as ResponderAutoRegistry
                        )
                    )
                }
            } else if (c.objectInstance != null) {
                list.add(
                    BoxedResponder(
                        c.objectInstance!! as Responder,
                        c.annotations.find { annotation -> annotation is ResponderAutoRegistry }!! as ResponderAutoRegistry
                    )
                )
            } else {
                println("[Error]: Responder $c 既没有提供主构建方法，也不是Object! 注册失败!")
            }
        }
        list.sortWith { r1, r2 -> r2.info.priority.i - r1.info.priority.i }
        for (r in list) {
            println("Responder ${r.info.name} 注册成功!")
        }
    }
}