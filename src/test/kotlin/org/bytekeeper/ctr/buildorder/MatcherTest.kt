package org.bytekeeper.ctr.buildorder

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MatcherTest {
    @Test
    fun shouldMatchOne() {
        val one = One("test")

        assertThat(one.matches(listOf("test"))).isTrue()
    }

    @Test
    fun shouldNotMatchOne() {
        val one = One("test")

        assertThat(one.matches(listOf("test2"))).isFalse()
    }

    @Test
    fun `one should not match terminal`() {
        val one = One("test")

        assertThat(one.matches(emptyList())).isFalse()
    }

    @Test
    fun shouldMatchAny() {
        assertThat(AnyItem.matches(listOf("oeuoeu"))).isTrue()
    }

    @Test
    fun `anyItem should not match no elements`() {
        assertThat(AnyItem.matches(emptyList())).isFalse()
    }

    @Test
    fun `should match sequence`() {
        val seq = Seq(One("a"), One("b"))

        assertThat(seq.matches(listOf("a", "b"))).isTrue()
    }

    @Test
    fun `should not match partial sequence`() {
        val seq = Seq(One("a"), One("b"))

        assertThat(seq.matches(listOf("a"))).isFalse()
    }

    @Test
    fun `should not match wrong sequence`() {
        val seq = Seq(One("b"))

        assertThat(seq.matches(listOf("a"))).isFalse()
    }

    @Test
    fun `should match at least one element`() {
        val alo = AtLeastOnce(One("a"))

        assertThat(alo.matches(listOf("a"))).isTrue()
    }

    @Test
    fun `atLeastOne should match repeated elements`() {
        val alo = AtLeastOnce(One("a"))

        assertThat(alo.matches(listOf("a", "a"))).isTrue()
    }

    @Test
    fun `atLeastOne should not match zero items`() {
        val alo = AtLeastOnce(One("a"))

        assertThat(alo.matches(emptyList())).isFalse()
    }

    @Test
    fun `atLeastOne should not match wrong item`() {
        val alo = AtLeastOnce(One("a"))

        assertThat(alo.matches(listOf("b"))).isFalse()
    }

    @Test
    fun `should match zero items`() {
        val zeroOrMore = ZeroOrMore(One("a"))

        assertThat(zeroOrMore.matches(emptyList())).isTrue()
    }

    @Test
    fun `zeroOrMore should match one item`() {
        val zeroOrMore = ZeroOrMore(One("a"))

        assertThat(zeroOrMore.matches(listOf("a"))).isTrue()
    }

    @Test
    fun `zeroOrMore should match multiple item`() {
        val zeroOrMore = ZeroOrMore(One("a"))

        assertThat(zeroOrMore.matches(listOf("a", "a"))).isTrue()
    }

    @Test
    fun `zeroOrMore should not match wrong item`() {
        val zeroOrMore = ZeroOrMore(One("a"))

        assertThat(zeroOrMore.matches(listOf("b"))).isFalse()
    }

    @Test
    fun `Should match complex sequence`() {
        val seq = Seq(One("a"), ZeroOrMore(One("b")), ZeroOrMore(ZeroOrMore(ZeroOrMore(One("b")))))

        assertThat(seq.matches(listOf("a", "b"))).isTrue()
    }

    @Test
    fun `oneOf should not match empty`() {
        val oneOf = OneOf(One("a"), One("b"))

        assertThat(oneOf.matches(emptyList())).isFalse()
    }

    @Test
    fun `oneOf should match first`() {
        val oneOf = OneOf(One("a"), One("b"))

        assertThat(oneOf.matches(listOf("a"))).isTrue()
    }

    @Test
    fun `oneOf should match successive`() {
        val oneOf = OneOf(One("a"), One("b"))

        assertThat(oneOf.matches(listOf("b"))).isTrue()
    }

    @Test
    fun `should not match item`() {
        val no = No("a", "b")

        assertThat(no.matches(listOf("a"))).isFalse()
    }

    @Test
    fun `should match any other item`() {
        val no = No("a", "c")

        assertThat(no.matches(listOf("b"))).isTrue()
    }

    @Test
    fun `should not match empty`() {
        val no = No("a")

        assertThat(no.matches(emptyList())).isFalse()
    }

    @Test
    fun `should ignore non-match`() {
        val m = Seq(Opt(One("a")), One("b"))

        assertThat(m.matches(listOf("b"))).isTrue()
    }

    @Test
    fun `should match optionally`() {
        val m = Seq(Opt(One("a")), One("b"))

        assertThat(m.matches(listOf("a", "b"))).isTrue()
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 9])
    fun `should match n times`(times: Int) {
        val m = Times(times, One("a"))

        assertThat(m.matches(List(times) { "a" })).isTrue()
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 9])
    fun `should not match n + 1 times`(times: Int) {
        val m = Times(times, One("a"))

        assertThat(m.matches(List(times + 1) { "a" })).isFalse()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 9])
    fun `should not match n - 1 times`(times: Int) {
        val m = Times(times, One("a"))

        assertThat(m.matches(List(times - 1) { "a" })).isFalse()
    }
}