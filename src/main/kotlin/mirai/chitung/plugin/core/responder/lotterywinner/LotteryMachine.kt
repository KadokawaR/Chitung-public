package mirai.chitung.plugin.core.responder.lotterywinner

import com.google.common.collect.HashBasedTable
import kotlinx.coroutines.delay
import mirai.chitung.plugin.administration.config.ConfigHandler
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.IdentityUtil
import mirai.chitung.plugin.utils.StandardTimeUtil
import mirai.chitung.plugin.utils.image.ImageCreater
import mirai.chitung.plugin.utils.sendTo
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.math.sqrt
import kotlin.random.Random

object LotteryMachine {
    private val timer = Timer(true)
    private val c4ActivationMap = ConcurrentHashMap<Long, Boolean>()
    private val bummerActivationTable: HashBasedTable<Long, Long, Boolean> = HashBasedTable.create()

    init {
        //每日6点定时清空C4触发标记
        timer.schedule(
            object : TimerTask() {
                override fun run() {
                    c4ActivationMap.clear()
                }
            },
            StandardTimeUtil.getStandardFirstTime(0, 0, 1),
            StandardTimeUtil.getPeriodLengthInMS(1, 0, 0, 0).toLong()
        )
    }

    private fun botPermissionChecker(event: GroupMessageEvent): Boolean =
        event.group.botPermission == MemberPermission.ADMINISTRATOR || event.group.botPermission == MemberPermission.OWNER

    private fun senderPermissionChecker(event: GroupMessageEvent): Boolean =
        event.sender.permission == MemberPermission.ADMINISTRATOR || event.sender.permission == MemberPermission.OWNER

    private fun sessionChecker(event: GroupMessageEvent): Boolean =
        bummerActivationTable.rowKeySet().contains(event.group.id) && bummerActivationTable.row(event.group.id)
            .containsKey(event.sender.id)

    suspend fun okBummer(event: PreprocessedMessageEvent) {
        if (LotteryBummerExclusion.userList.contains(event.sender().id)) {
            event.createAt().plus("您已经开启Bummer保护。").sendTo(event)
        }
        if (!GroupConfigManager.lotteryConfig(event.body as GroupMessageEvent)) {
            "本群暂未开启Bummer功能。".sendTo(event)
        }
        if (!ConfigHandler.getINSTANCE().config.groupFC.isLottery) {
            "机器人暂未开启Bummer功能。".sendTo(event)
        }
        if (botPermissionChecker(event.body)) {
            if (sessionChecker(event.body)) return
            //抽取倒霉蛋
            var candidates = event.group()!!.members.stream().filter { member: NormalMember ->
                (((member.permission == MemberPermission.MEMBER))
                        &&
                        !LotteryBummerExclusion.userList.contains(event.sender().id))
            }.collect(Collectors.toList())
            if (candidates.isEmpty()) {
                "要么都是管理员要么都没有人玩Bummer了？别闹。".sendTo(event)
            } else {
                //排除官方Bot，最好不要戳到这群“正规军”
                candidates = candidates.stream().filter { member: NormalMember ->
                    !IdentityUtil.isOfficialBot(
                        member.id
                    )
                }.collect(Collectors.toList())
                if (candidates.isEmpty()) {
                    "你想让我去禁言官方Bot？不可以的吧。".sendTo(event)
                }
            }
            val victim = candidates[Random.nextInt(candidates.size)]
            victim.mute(120)

            //如果发送者不是管理员，那么发送者也将被禁言
            if (!senderPermissionChecker(event.body)) {
                event.member()!!.mute(120)
            }
            if (victim.id == event.sender().id) {
                "Ok Bummer! ${victim.nick}\n${event.sender().nick}尝试随机极限一换一。他成功把自己换出去了！".sendTo(event)
            } else if ((senderPermissionChecker(event.body))) {
                //如果发送者是管理员，那么提示
                PlainText("Ok Bummer! " + victim.nick + "\n管理员")
                    .plus(At(event.sender().id))
                    .plus(PlainText(" 随机带走了 "))
                    .plus(At(victim.id)).sendTo(event)
            } else {
                PlainText("Ok Bummer! " + victim.nick + "\n")
                    .plus(At(event.sender().id))
                    .plus(PlainText(" 以自己为代价随机带走了 "))
                    .plus(At(victim.id)).sendTo(event)
            }
            bummerActivationTable.put(event.group()!!.id, event.body.sender.id, true)
            delay(StandardTimeUtil.getPeriodLengthInMS(0, 0, 2, 0).toLong())
            bummerActivationTable.remove(
                event.group()!!.id,
                event.sender().id
            )
        } else {
            "七筒目前还没有管理员权限，请授予七筒权限解锁更多功能。".sendTo(event)
        }
    }

    suspend fun okWinner(event: PreprocessedMessageEvent) {
        //获取当日幸运数字
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH] + 1
        val date = calendar[Calendar.DATE]
        val numOfTheDay = (year + (month * 10000) + (date * 1000000)) * 100000000000L / event.group()!!.id

        //获取当日幸运儿
        //排除掉官方Bot，不要去动它们
        val candidates = event.group()!!.members.stream().filter { member: NormalMember ->
            !IdentityUtil.isOfficialBot(
                member.id
            )
        }.collect(Collectors.toList())
        val guyOfTheDay = numOfTheDay % candidates.size
        "Ok Winner! ${candidates[Math.toIntExact(guyOfTheDay)].nick}".sendTo(event)
        ImageCreater.sendImage(
            ImageCreater.createWinnerImage(candidates[Math.toIntExact(guyOfTheDay)]),
            event.body as GroupMessageEvent
        )
    }

    suspend fun okC4(event: PreprocessedMessageEvent) {
        if (!GroupConfigManager.lotteryConfig(event.body as GroupMessageEvent)) {
            "本群暂未开启C4功能。".sendTo(event)
        }
        if (!ConfigHandler.getINSTANCE().config.groupFC.isLottery) {
            "机器人暂未开启C4功能。".sendTo(event)
        }
        if (botPermissionChecker(event.body)) {
            if (!c4ActivationMap.getOrDefault(event.group()!!.id, false)) {
                val ratio = 1.0 / sqrt(event.group()!!.members.size.toDouble())
                if (Random.nextDouble() < ratio) {
                    event.group()!!.settings.isMuteAll = true
                    "中咧！".sendTo(event)
                    At(event.sender().id).plus("成功触发了C4！大家一起恭喜TA！").sendTo(event)
                    c4ActivationMap[event.group()!!.id] = true
                    //设置5分钟后解禁
                    delay(StandardTimeUtil.getPeriodLengthInMS(0, 0, 5, 0).toLong())
                    event.group()!!.settings.isMuteAll = false
                } else {
                    At(event.sender().id).plus("没有中！").sendTo(event)
                }
            } else {
                At(event.sender().id).plus("今日的C4已经被触发过啦！请明天再来尝试作死！").sendTo(event)
            }
        } else {
            "七筒目前还没有管理员权限，请授予七筒权限解锁更多功能。".sendTo(event)
        }
    }
}