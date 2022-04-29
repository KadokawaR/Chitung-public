package mirai.chitung.plugin.core.responder.dice

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.*
import java.util.regex.Pattern

@ResponderAutoRegistry("掷骰子", RespondFrom.GroupAndFriend, Priority.Lowest)
object DiceResponder : Responder {
    private val reg =
        Regex("(((/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7}))|(\\.([1-9]\\d{0,2})([dD])[1-9]\\d{0,7})|(\\.([dD])[1-9]\\d{0,7}))")
    private val PATTERN_COMMON_COMMAND = Pattern.compile("(/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7})")
    private val PATTERN_DND = Pattern.compile("\\.([1-9]\\d{0,2})([dD])[1-9]\\d{0,7}")
    private val PATTERN_DND_SINGLE_ROLL = Pattern.compile("\\.([dD])[1-9]\\d{0,7}")
    private val CAPTURE_PATTERN_COMMON_COMMAND = Pattern.compile("(/dice|/d|/Dice|/D)\\s?([1-9]\\d{0,7})")
    private val CAPTURE_PATTERN_DND = Pattern.compile("\\.([1-9]\\d{0,2})([dD])([1-9]\\d{0,7})")
    private val CAPTURE_PATTERN_DND_SINGLE_ROLL = Pattern.compile("\\.([dD])([1-9]\\d{0,7})")
    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(executeDiceCommand(event.rawText)!!)
                }
            }
            return true
        }
        return false
    }

    private fun executeDiceCommand(message: String): String? {
        if (PATTERN_COMMON_COMMAND.matcher(message).matches()) {
            return Dice.create(captureFromPatternCommon(message)).result()
        } else {
            if (PATTERN_DND.matcher(message).matches()) {
                return Dice.create(captureFromPatternDND(message)[1], captureFromPatternDND(message)[0]).result()
            } else if (PATTERN_DND_SINGLE_ROLL.matcher(message).matches()) {
                return Dice.create(captureFromPatternDNDSingleRoll(message)).result()
            }
        }
        return null
    }

    private fun captureFromPatternCommon(input: String): Int {
        val matcher = CAPTURE_PATTERN_COMMON_COMMAND.matcher(input)
        matcher.find()
        return matcher.group(2).toInt()
    }

    private fun captureFromPatternDND(input: String): List<Int> {
        val matcher = CAPTURE_PATTERN_DND.matcher(input)
        matcher.find()
        val captured: MutableList<Int> = mutableListOf()
        captured.add(Integer.valueOf(matcher.group(1)))
        captured.add(Integer.valueOf(matcher.group(3)))
        return captured
    }

    private fun captureFromPatternDNDSingleRoll(input: String): Int {
        val matcher = CAPTURE_PATTERN_DND_SINGLE_ROLL.matcher(input)
        matcher.find()
        return matcher.group(2).toInt()
    }
}