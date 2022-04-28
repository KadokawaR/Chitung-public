package mirai.chitung.plugin.core.responder.basic
import com.google.gson.Gson
import mirai.chitung.plugin.utils.fromResourceReadText
import java.util.*

object BasicReplyCluster{
    var dataHolder: DataHolder = Gson().fromJson("/cluster/autoreply.json".fromResourceReadText()!!, DataHolder::class.java)

    private fun pickReply(type: BasicReplyType): String {
        val selectedLines: TreeMap<Double, String> = when (type) {
            BasicReplyType.AntiOverWatch -> dataHolder.antiOverwatchGameReplyLines
            BasicReplyType.GoodBye -> dataHolder.goodbyeReplyLines
            BasicReplyType.AntiDirtyWord -> dataHolder.antiDirtyWordsReplyLines
        }
        val tailMap: SortedMap<Double, String> = selectedLines.tailMap(selectedLines.lastKey() * Math.random(), false)
        return selectedLines[tailMap.firstKey()]!!
    }

    //回复消息
    fun reply(type: BasicReplyType): String {
        return pickReply(type)
    }
}

enum class BasicReplyType {
    AntiOverWatch, AntiDirtyWord, GoodBye
}

data class DataHolder(var goodbyeReplyLines: TreeMap<Double, String>, var antiDirtyWordsReplyLines: TreeMap<Double, String>, var antiOverwatchGameReplyLines: TreeMap<Double, String>)


