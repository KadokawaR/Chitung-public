package mirai.chitung.plugin.core.responder.overwatch

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import com.google.gson.*
import mirai.chitung.plugin.utils.readTextFromResource
import java.lang.reflect.Type

object HeroVoiceLineCluster {
    private val data =
        GsonBuilder().registerTypeAdapter(Multimap::class.java, HeroLinesMultimapTypeAdapter<Any?, Any?>()).create()
            .fromJson("/cluster/herolines.json".readTextFromResource(), OverwatchHeroVoiceLineData::class.java)

    //随机挑选大招台词
    fun pickUltimateAbilityHeroLineByRandomHero(): String {
        val randomHero = Hero.values().random()
        val lines = data.ultimateAbilityHeroLines[randomHero]
        return lines.random()
    }

    //随机挑选某位英雄的大招台词
    fun pickUltimateAbilityHeroLine(hero: Hero): String {
        val lines = data.ultimateAbilityHeroLines[hero]
        return lines.random()
    }
}

enum class Hero {
    ANA, ASHE, BAPTISTE, BASTION, BRIGITTE, DVA, DOOMFIST, ECHO, GENJI, HANZO, JUNKRAT, LUCIO, MCCREE, MEI, MERCY, MOIRA, ORISA, PHARAH, REAPER, REINHARDT, ROADHOG, SIGMA, SOLDIER_76, SOMBRA, SYMMETRA, TORBJORN, TRACER, WIDOWMAKER, WINSTON, WRECKING_BALL, ZARYA, ZENYATTA
}

internal data class OverwatchHeroVoiceLineData(
    val ultimateAbilityHeroLines: Multimap<Hero, String>,
    var commonHeroLines: Multimap<Hero, String>
)

internal class HeroLinesMultimapTypeAdapter<K, V> :
    JsonSerializer<Multimap<K, V>>,
    JsonDeserializer<Multimap<K, V>> {
    override fun serialize(
        heroStringMultimap: Multimap<K, V>,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        val `object` = JsonArray()
        val heroCollectionMap = heroStringMultimap.asMap()
        for (hero in Hero.values()) {
            val jsonHeroLinesCollection = JsonObject()
            val jsonHeroLines = JsonArray()
            for (v in heroCollectionMap[hero as K]!!) {
                jsonHeroLines.add(v as String)
            }
            jsonHeroLinesCollection.add(hero.name, jsonHeroLines)
            `object`.add(jsonHeroLinesCollection)
        }
        return `object`
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext
    ): Multimap<K, V> {
        val heroStringMultimap: Multimap<K, V> = MultimapBuilder.hashKeys().arrayListValues().build()
        for (element in jsonElement.asJsonArray) {
            val jsonHeroPack = element as JsonObject
            val heroLines: MutableCollection<String> = ArrayList()
            val hero = Hero.valueOf((jsonHeroPack.keySet().toTypedArray()[0] as String))
            for (value in jsonHeroPack[hero.name].asJsonArray) {
                heroLines.add(value.asJsonPrimitive.asString)
            }
            for (line in heroLines) {
                heroStringMultimap.put(hero as K, line as V)
            }
        }
        return heroStringMultimap
    }
}