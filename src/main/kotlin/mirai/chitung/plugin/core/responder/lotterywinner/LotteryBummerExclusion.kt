package mirai.chitung.plugin.core.responder.lotterywinner

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.*
import mirai.chitung.plugin.utils.fileutils.Touch
import mirai.chitung.plugin.utils.fileutils.Write
import mirai.chitung.plugin.utils.readTextFromFile
import mirai.chitung.plugin.utils.sendTo
import net.mamoe.mirai.message.data.At
import java.io.File


@ResponderAutoRegistry("BummerExclusion", RespondFrom.GroupAndFriend)
object LotteryBummerExclusion: Responder {
    private val EXCLUSION_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Chitung" + File.separator + "exclusion.json"
    var userList: ArrayList<Long>

    init {
        if(Touch.file(EXCLUSION_PATH)) {
            userList = arrayListOf()
            writeRecord()
        } else {
            userList = readRecord()
        }
    }

    private fun readRecord(): ArrayList<Long> = Gson().fromJson(EXCLUSION_PATH.readTextFromFile(), object: TypeToken<ArrayList<Long>>(){}.type)

    private fun writeRecord() = Write.cover(GsonBuilder().setPrettyPrinting().create().toJson(userList, object: TypeToken<ArrayList<Long>>(){}.type), EXCLUSION_PATH)

    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if(event.rawText.equals("/open bummer", ignoreCase = true) || event.rawText.equals("/close bummer", ignoreCase = true)){
            coroutineScope {
                launch {
                    val isOpen: Boolean = event.contentHas("/open")
                    if (isOpen) {
                        if (!userList.contains(event.sender().id))
                            userList.add(event.sender().id)
                        writeRecord()
                        if (event.isFromGroup()) {
                            At(event.sender().id).plus("已为您打开Bummer保护。").sendTo(event)
                        } else {
                            "已为您打开Bummer保护。".sendTo(event)
                        }
                    } else {
                        if (userList.contains(event.sender().id)) {
                            if (event.isFromGroup()) {
                                At(event.sender().id).plus("您没有开启Bummer保护。").sendTo(event)
                            } else {
                                "您没有开启Bummer保护。".sendTo(event)
                            }
                        }
                        userList.remove(event.sender().id)
                        writeRecord()
                        if (event.isFromGroup()) {
                            At(event.sender().id).plus("已为您关闭Bummer保护。").sendTo(event)
                        } else {
                            "已为您关闭Bummer保护。".sendTo(event)
                        }
                    }
                }
            }
            return true
        }
        return false
    }
}