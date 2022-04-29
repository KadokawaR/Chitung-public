package mirai.chitung.plugin.core.responder.dice

import java.util.*

class Dice(val bound: Int, val repeat: Int) {
    var result: MutableList<Int> = mutableListOf()

    companion object {
        fun create(bound: Int, repeat: Int = 1): Dice {
            val dice = Dice(bound, repeat)
            dice.roll()
            return dice
        }

        fun standard(): Dice = create(6)

        fun dnd(): Dice = create(100)

        fun coc(): Dice = create(100)

        fun coin(): Dice = create(2)
    }
}

/**
 * 扔骰子操作，用这个方法来重新扔一次骰子
 */
fun Dice.roll() {
    if (result.isNotEmpty()) result = ArrayList()
    for (i in 0 until repeat) {
        result.add(Random(System.nanoTime()).nextInt(bound) + 1)
    }
}

fun Dice.result(): String = "您掷出的点数是:${this.result.joinToString(" ")}"

fun Dice.toList(): List<Int> = this.result