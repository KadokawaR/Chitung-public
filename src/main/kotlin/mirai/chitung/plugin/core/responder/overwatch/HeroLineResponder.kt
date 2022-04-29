package mirai.chitung.plugin.core.responder.overwatch

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mirai.chitung.plugin.core.responder.PreprocessedMessageEvent
import mirai.chitung.plugin.core.responder.Responder
import mirai.chitung.plugin.core.responder.ResponderAutoRegistry

@ResponderAutoRegistry("守望先锋大招台词")
object HeroLineResponder : Responder {
    private val reg = Regex("((大招)|(英雄不朽))")
    override fun receive(event: PreprocessedMessageEvent): Boolean {
        if (reg.matches(event.rawText)) {
            runBlocking {
                launch {
                    event.body.subject.sendMessage(HeroVoiceLineCluster.pickUltimateAbilityHeroLineByRandomHero())
                }
            }
            return true
        }
        return false
    }
}