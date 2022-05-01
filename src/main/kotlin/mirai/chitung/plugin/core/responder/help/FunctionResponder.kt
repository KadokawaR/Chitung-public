package mirai.chitung.plugin.core.responder.help

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.administration.config.ConfigHandler
import mirai.chitung.plugin.administration.config.FunctionConfig
import mirai.chitung.plugin.core.groupconfig.GroupConfig
import mirai.chitung.plugin.core.groupconfig.GroupConfigManager
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.getResourceByStream
import mirai.chitung.plugin.utils.image.ImageCreater
import mirai.chitung.plugin.utils.image.ImageSender
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

@ResponderAutoRegistry("功能", RespondFrom.GroupAndFriend)
object FunctionResponder : Responder {
    data class Position(var X: Int, var Y: Int)

    private var positions = arrayOf(
        Position(788, 1352),
        Position(34, 233),
        Position(34, 1135),
        Position(285, 146),
        Position(286, 395),
        Position(286, 933),
        Position(536, 145),
        Position(536, 491)
    )

    private fun String.getImage(): BufferedImage = ImageIO.read(this.getResourceByStream()!!)

    enum class Type {
        Friend, Group
    }

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.contentEquals("查看功能") || event.contentEquals("/funct")) {
            coroutineScope {
                launch {
                    if (event.isFromGroup()) {
                        ImageSender.sendImageFromBufferedImage(
                            event.group(),
                            assemblePic(Type.Group, event.group()!!.id)
                        )
                    } else {
                        ImageSender.sendImageFromBufferedImage(event.sender(), assemblePic(Type.Friend, 0L))
                    }
                }
            }
            return true
        }
        return false
    }

    private fun getRC(type: Type): FunctionConfig {
        return if (type == Type.Friend) {
            ConfigHandler.getINSTANCE().config.friendFC
        } else ConfigHandler.getINSTANCE().config.groupFC
    }

    private fun getGC(ID: Long): GroupConfig {
        return GroupConfigManager.getGroupConfig(ID)
    }

    private fun assemblePic(type: Type, groupID: Long): BufferedImage {
        val filePath = "/pics/function/help-0"
        val images = arrayOfNulls<BufferedImage>(8)
        val paths = arrayOfNulls<String>(8)
        for (i in paths.indices) {
            paths[i] = filePath + i
        }
        if (type == Type.Friend) {
            if (!getRC(type).isResponder) {
                paths[1] += "-closed"
                paths[3] += "-closed"
            }
            paths[2] += "-closed"
            paths[4] += "-closed"
            paths[5] += "-closed"
            if (!getRC(type).isCasino || !getRC(type).isGame) {
                paths[6] += "-closed"
            }
            if (!getRC(type).isFish || !getRC(type).isGame) {
                paths[7] += "-closed"
            }
        } else {
            if (!getRC(type).isResponder || !getGC(groupID).isResponder) {
                paths[1] += "-closed"
                paths[3] += "-closed"
            }
            if (!getRC(type).isGame || !getGC(groupID).isGame) {
                paths[2] += "-closed"
            }
            if (!getRC(type).isLottery || !getGC(groupID).isLottery) {
                paths[5] += "-closed"
            }
            if (!getRC(type).isCasino || !getRC(type).isGame || !getGC(groupID).isCasino || !getGC(groupID).isGame) {
                paths[6] += "-closed"
            }
            if (!getRC(type).isFish || !getRC(type).isGame || !getGC(groupID).isFish || !getGC(groupID).isGame) {
                paths[7] += "-closed"
            }
        }
        for (i in images.indices) {
            paths[i] += ".png"
        }
        for (i in images.indices) {
            images[i] = paths[i]!!.getImage()
            println(images[i])
        }
        var result = images[0]
        for (i in 1 until images.size) {
            result = ImageCreater.addImage(
                result,
                images[i],
                positions[i].X * images[0]!!.width / positions[0].X,
                positions[i].Y * images[0]!!.height / positions[0].Y
            )
        }
        return result!!
    }
}