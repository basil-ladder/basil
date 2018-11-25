package org.bytekeeper.ctr

import org.bytekeeper.ctr.proj.BotProjections
import org.bytekeeper.ctr.proj.EloProjections
import org.bytekeeper.ctr.proj.GameResultsProjections
import org.springframework.stereotype.Component

@Component
class Orchestration(events: Events,
                    commands: Commands,
                    eloProjections: EloProjections,
                    gameResultsProjections: GameResultsProjections,
                    botProjections: BotProjections,
                    rankingsPublisher: RankingsPublisher) {
    init {
        events.register(gameResultsProjections::gameEnded)
        events.register(gameResultsProjections::gameCrashed)
        events.register(botProjections::onGameEnded)
        events.register(botProjections::onGameCrashed)
        events.register(eloProjections::onEloUpdated)

        commands.register(eloProjections::handle)
        commands.register(rankingsPublisher::handle)
        commands.register(gameResultsProjections::handle)
        commands.register(botProjections::handle)
    }
}