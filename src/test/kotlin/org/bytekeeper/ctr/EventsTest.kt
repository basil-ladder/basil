package org.bytekeeper.ctr

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class EventsTest {
    @Test
    fun `should run remaining event handlers after one fails`() {
        // GIVEN
        val events = Events()
        val secondHandlerCalled = CountDownLatch(1)
        events.register(String::class.java) { throw IllegalStateException("broken handler") }
        events.register(String::class.java) { secondHandlerCalled.countDown() }

        // WHEN
        events.post("event")

        // THEN
        assertThat(secondHandlerCalled.await(5, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun `should run remaining command handlers after one fails`() {
        // GIVEN
        val commands = Commands()
        var secondHandlerCalled = false
        commands.register(String::class.java) { throw IllegalStateException("broken handler") }
        commands.register(String::class.java) { secondHandlerCalled = true }

        // WHEN
        commands.handle("command")

        // THEN
        assertThat(secondHandlerCalled).isTrue()
    }
}
