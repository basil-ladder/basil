package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.bytekeeper.ctr.entity.BasilSourceBot
import org.bytekeeper.ctr.entity.BasilSourceBotRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import java.time.Instant

class BasilBotServiceTest {
    private val basilSourceBotRepository = mock<BasilSourceBotRepository>()
    private val sut = BasilBotService(basilSourceBotRepository)

    @Test
    fun `should update new bot`() {
        // GIVEN

        // WHEN
        val updated = sut.update("test", "1", Instant.now())

        // THEN
        assertThat(updated).isNotNull()
        val captor = ArgumentCaptor.forClass(BasilSourceBot::class.java)
        verify(basilSourceBotRepository).save(captor.capture())
        assertThat(captor.value).extracting("lastUpdated").doesNotContainNull()
    }

    @Test
    fun `should update on changed hash`() {
        // GIVEN
        given(basilSourceBotRepository.findByName("test")).willReturn(BasilSourceBot(1, "test", null, "2"))
        val updateTime = Instant.now()

        // WHEN
        val updated = sut.update("test", "1", updateTime)

        // THEN
        assertThat(updated).isEqualTo(updateTime)
        val captor = ArgumentCaptor.forClass(BasilSourceBot::class.java)
        verify(basilSourceBotRepository).save(captor.capture())
        assertThat(captor.value).extracting("lastUpdated").doesNotContainNull()
    }

    @Test
    fun `should not update on same hash`() {
        // GIVEN
        given(basilSourceBotRepository.findByName("test")).willReturn(BasilSourceBot(1, "test", Instant.MIN, "1"))

        // WHEN
        val updated = sut.update("test", "1", Instant.now())

        // THEN
        assertThat(updated).isEqualTo(Instant.MIN)
        val captor = ArgumentCaptor.forClass(BasilSourceBot::class.java)
        verify(basilSourceBotRepository).save(captor.capture())
        assertThat(captor.value).extracting("lastUpdated").allMatch { it == Instant.MIN }
    }
}