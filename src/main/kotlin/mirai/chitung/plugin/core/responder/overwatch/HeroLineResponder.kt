package mirai.chitung.plugin.core.responder.overwatch

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry
import mirai.chitung.plugin.core.responder.matches
import mirai.chitung.plugin.utils.sendTo

@ResponderAutoRegistry("守望先锋大招台词")
object HeroLineResponder : Responder {
    private val reg = Regex("((大招)|(英雄不朽))")
    override suspend fun receive(event: PreprocessedMessageEvent): Boolean {
        if (event.matches(reg)) {
            coroutineScope {
                launch {
                    HeroVoiceLineCluster.pickUltimateAbilityHeroLineByRandomHero().sendTo(event)
                }
            }
            return true
        }
        return false
    }
}