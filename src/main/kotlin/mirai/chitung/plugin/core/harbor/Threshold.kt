package mirai.chitung.plugin.core.harbor

import java.util.concurrent.ConcurrentHashMap

internal class Threshold(private val limit: Int) {
    val data: MutableMap<Long, Int> = ConcurrentHashMap()

    fun clear() {
        data.clear()
    }

    fun count(id: Long) {
        data[id] = data.getOrDefault(id, 0) + 1
    }

    operator fun get(id: Long): Int {
        return data.getOrDefault(id, 0)
    }

    fun reachLimit(id: Long): Boolean {
        return data.getOrDefault(id, 0) >= limit
    }
}
