package org.bytekeeper.ctr.proj

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.*
import org.bytekeeper.ctr.SCMapPool.Companion.poolSscait
import org.bytekeeper.ctr.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class BotProjectionsTest {
    private lateinit var sut: BotProjections

    @Mock
    private lateinit var botRepository: BotRepository

    @Mock
    private lateinit var botHistoryRepository: BotHistoryRepository

    @Mock
    private lateinit var events: Events

    private val botA = Bot(id = 1, name = "A", race = Race.RANDOM, botType = "")
    private val botB = Bot(id = 2, name = "B", race = Race.RANDOM, botType = "")
    private val gameResult = GameResult(
            id = UUID.randomUUID(),
            gameRealtime = 0.0,
            time = Instant.now(),
            mapPool = "",
            map = "",
            botA = botA,
            raceA = Race.ZERG,
            botB = botB,
            raceB = Race.TERRAN,
            gameHash = ""
    )

    private val pool = poolSscait
    private val map: SCMap = pool.maps[0]

    @BeforeEach
    fun setup() {
        sut = BotProjections(botRepository, botHistoryRepository, events)
        given(botRepository.getById(ArgumentMatchers.anyLong()))
                .willAnswer {
                    when (it.arguments[0]) {
                        1L -> botA
                        2L -> botB
                        else -> throw IllegalStateException()
                    }
                }
    }

    @Test
    fun shouldWriteEloHistory() {

        (1..100).map {
            for (i in 1..50) {
                if (Math.random() < 0.5)
                    sut.onGameWon(GameWon(gameResult, botA, botB))
                else
                    sut.onGameWon(GameWon(gameResult, botB, botA))
            }
        }

        verify(events, times(100 * 50 * 2)).post(any<EloUpdated>())
    }

    @Test
    fun shouldRegisterCrash() {
        // GIVEN

        // WHEN
        sut.onGameCrashed(GameCrashed(UUID.randomUUID(), botA, Race.ZERG, botB, Race.TERRAN, pool, map, true, false, Instant.now(), 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("crashesSinceUpdate", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashesSinceUpdate", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameEnded() {
        // GIVEN

        // WHEN
        sut.onGameEnded(GameEnded(UUID.randomUUID(), botA, Race.ZERG, botB, Race.PROTOSS, pool, map, Instant.now(), 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameFailedToStart() {
        // GIVEN

        // WHEN
        sut.onGameFailedToStart(GameFailedToStart(UUID.randomUUID(), botA, Race.PROTOSS, botB, Race.TERRAN, pool, map, Instant.now(), ""))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGametimeOut() {
        // GIVEN

        // WHEN
        sut.onGameTimedOut(GameTimedOut(UUID.randomUUID(), botA, Race.TERRAN, botB, Race.ZERG, botA, 1, 0, pool, map, Instant.now(), false, true, 0.0, "", null))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 0)
    }

    @Test
    fun shouldRegisterGameWon() {
        // GIVEN

        // WHEN
        sut.onGameWon(GameWon(gameResult, botA, botB))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("crashed", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("played", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("played", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("won", 1)
        assertThat(botB).hasFieldOrPropertyWithValue("won", 0)
        assertThat(botA).hasFieldOrPropertyWithValue("lost", 0)
        assertThat(botB).hasFieldOrPropertyWithValue("lost", 1)
    }

    @Test
    fun `should handle bot update`() {
        // GIVEN
        sut.onGameCrashed(GameCrashed(UUID.randomUUID(), botA, Race.ZERG, botB, Race.TERRAN, pool, map, true, false, Instant.now(), 0.0, "", null))

        // WHEN
        sut.onBotUpdated(BotBinaryUpdated(botA, Instant.now(), emptyList()))

        // THEN
        assertThat(botA).hasFieldOrPropertyWithValue("crashed", 1)
        assertThat(botA).hasFieldOrPropertyWithValue("crashesSinceUpdate", 0)
    }

    @Test
    fun `should write history entry for bot update`() {
        // GIVEN
        val updateTime = Instant.now()

        // WHEN
        sut.onBotUpdated(BotBinaryUpdated(botA, updateTime, emptyList()))

        // THEN
        val captor = ArgumentCaptor.forClass(BotHistory::class.java)
        verify(botHistoryRepository).save(captor.capture())
        assertThat(captor.value).extracting { it.bot to it.time }
                .isEqualTo(botA to updateTime)
    }
}