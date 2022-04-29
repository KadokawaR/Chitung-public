package mirai.chitung.plugin.core.harbor

import com.google.common.collect.Maps
import mirai.chitung.plugin.utils.IdentityUtil
import mirai.chitung.plugin.utils.StandardTimeUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Harbor {
    private val MIN_THRESHOLDS: MutableMap<String, Threshold?> = ConcurrentHashMap()
    private val DAY_THRESHOLDS: MutableMap<String, Threshold?> = ConcurrentHashMap()
    private val thresholdReset1 = Timer(true)
    private val thresholdReset2 = Timer(true)

    init {
        thresholdReset1.schedule(
            object : TimerTask() {
                override fun run() {
                    for (threshold in MIN_THRESHOLDS.values) {
                        threshold!!.clear()
                    }
                }
            }, StandardTimeUtil.getPeriodLengthInMS(0, 0, 0, 1).toLong(),
            StandardTimeUtil.getPeriodLengthInMS(0, 0, 1, 0).toLong()
        )
        thresholdReset2.schedule(
            object : TimerTask() {
                override fun run() {
                    MIN_THRESHOLDS.clear()
                    DAY_THRESHOLDS.clear()
                }
            }, StandardTimeUtil.getStandardFirstTime(0, 0, 1),
            StandardTimeUtil.getPeriodLengthInMS(1, 0, 0, 0).toLong()
        )
    }

    private fun acquire(requestInfo: PortRequestInfo): Array<Threshold?> {
        if (MIN_THRESHOLDS[requestInfo.tag] == null) {
            MIN_THRESHOLDS[requestInfo.tag] = Threshold(requestInfo.minute_limit)
        }
        if (DAY_THRESHOLDS[requestInfo.tag] == null) {
            DAY_THRESHOLDS[requestInfo.tag] = Threshold(requestInfo.daily_limit)
        }
        return arrayOf(
            MIN_THRESHOLDS[requestInfo.tag],
            DAY_THRESHOLDS[requestInfo.tag]
        )
    }

    @JvmStatic
    fun isReachingPortLimit(requestInfo: PortRequestInfo, id: Long): Boolean {
        val thresholds = acquire(requestInfo)
        return thresholds[0]!!.reachLimit(id) || thresholds[1]!!.reachLimit(id)
    }

    @JvmStatic
    fun count(requestInfo: PortRequestInfo, id: Long) {
        val thresholds = acquire(requestInfo)
        thresholds[0]!!.count(id)
        thresholds[1]!!.count(id)
    }

    @JvmStatic
    fun count(event: MessageEvent) {
        if (event is GroupMessageEvent) {
            count(PortRequestInfos.GROUP_MINUTE, event.group.id)
        }
        count(PortRequestInfos.PERSONAL, event.sender.id)
        count(PortRequestInfos.TOTAL_DAILY, 0)
    }

    @JvmStatic
    fun getMinutePortRecordById(requestInfo: PortRequestInfo, id: Long): Int {
        return acquire(requestInfo)[0]!![id]
    }

    @JvmStatic
    fun getDailyPortRecordById(requestInfo: PortRequestInfo, id: Long): Int {
        return acquire(requestInfo)[1]!![id]
    }

    @JvmStatic
    fun getMinutePortRecord(requestInfo: PortRequestInfo): Map<Long, Int> {
        return Maps.newHashMap(acquire(requestInfo)[0]!!.data)
    }

    @JvmStatic
    fun getDailyPortRecord(requestInfo: PortRequestInfo): Map<Long, Int> {
        return Maps.newHashMap(acquire(requestInfo)[1]!!.data)
    }

    @JvmStatic
    fun clearPortRecordManually(requestInfo: PortRequestInfo) {
        val thresholds = acquire(requestInfo)
        thresholds[0]!!.clear()
        thresholds[1]!!.clear()
    }

    @JvmStatic
    fun isReachingPortLimit(event: MessageEvent): Boolean {
        if (IdentityUtil.isAdmin(event.sender.id)) return false
        if (isReachingPortLimit(PortRequestInfos.TOTAL_DAILY, 0)) return true
        return if (event is GroupMessageEvent) {
            isReachingPortLimit(
                PortRequestInfos.PERSONAL,
                event.sender.id
            ) || isReachingPortLimit(PortRequestInfos.GROUP_MINUTE, event.group.id)
        } else {
            isReachingPortLimit(PortRequestInfos.PERSONAL, event.sender.id)
        }
    }
}
