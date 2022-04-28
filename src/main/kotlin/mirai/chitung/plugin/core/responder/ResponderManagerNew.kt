package mirai.chitung.plugin.core.responder

import mirai.chitung.plugin.core.harbor.Harbor.isReachingPortLimit
import mirai.chitung.plugin.core.harbor.PortRequestInfos
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and
import kotlin.reflect.full.*


object ResponderManagerNew {
    private var map: MutableMap<Responder,ResponderAutoRegistry> = ConcurrentHashMap()

    fun MessageEvent.sendToResponderManager(){
        if (this.isQualifiedForResponder()) {
            PreprocessedMessageEvent(this).handle()
        }
    }

    private fun MessageEvent.isQualifiedForResponder(): Boolean{
        return when {
            reachLimit(this) -> false
            this is GroupMessageEvent -> true
            this is FriendMessageEvent -> true
            else -> false
        }
    }

    private fun reachLimit(event: MessageEvent): Boolean {
        if (isReachingPortLimit(PortRequestInfos.TOTAL_DAILY, 0L)) return true
        if (event is GroupMessageEvent) {
            if (isReachingPortLimit(PortRequestInfos.GROUP_MINUTE, event.subject.id)) return true
        }
        return isReachingPortLimit(PortRequestInfos.PERSONAL, event.subject.id)
    }

    private fun PreprocessedMessageEvent.handle(){
        for((k,v) in map){
            if (v.from.typeBit.and(this.typeBitMask)>0){
                if(k.receive(this)){
                    break
                }
            }
        }
    }

    fun setup(){
        val reflection = Reflections(ConfigurationBuilder()
            .forPackage("mirai.chitung")
            .setScanners(Scanners.TypesAnnotated))
        val classes = reflection.getTypesAnnotatedWith(ResponderAutoRegistry::class.java).map { clazz -> clazz.kotlin }
        for(c in classes){
            if(!c.isSubclassOf(Responder::class)){
                println("[Error]: Responder $c 不是一个 Responder! 注册失败!")
            }
            if(c.primaryConstructor!=null){
                if(c.primaryConstructor!!.parameters.isNotEmpty()){
                    println("[Error]: Responder $c 的主构建方法方法! 注册失败!")
                }
                else{
                    map[c.primaryConstructor!!.call() as Responder] = c.annotations.find { annotation -> annotation is ResponderAutoRegistry }!! as ResponderAutoRegistry
                }
            }
            else if(c.objectInstance!=null){
                map[c.objectInstance!! as Responder] = c.annotations.find { annotation -> annotation is ResponderAutoRegistry }!! as ResponderAutoRegistry
            }
            else{
                println("[Error]: Responder $c 既没有提供主构建方法，也不是Object! 注册失败!")
            }
            for(e in map.entries){
                println("${e.value.name} 触发类型:${e.value.from} 优先级:${e.value.priority}")
            }

        }
    }


}