package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.pickNext
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class PickNextTipUnitTest {

    private val random = Random(42)
    private val counts = mutableMapOf<String, Int>()
    private var lastShownKey: String? = null

    @Test
    fun twoCyclesShowEveryTipExactlyTwice() {
        val pool = tips("a", "b", "c", "d", "e")

        val drawn = draw(pool, times = 2 * pool.size)

        assertEquals(pool.associate { it.summary to 2 }, drawn.groupingBy { it }.eachCount())
    }

    @Test
    fun neverRepeatsTheLastShownTip() {
        val drawn = draw(tips("a", "b"), times = 50)

        drawn.zipWithNext().forEach { (previous, next) -> assertNotEquals(previous, next) }
    }

    @Test
    fun singleTipPoolKeepsReturningIt() {
        assertEquals(listOf("a", "a", "a"), draw(tips("a"), times = 3))
    }

    @Test
    fun emptyPoolReturnsNull() {
        assertNull(pickNext(emptyList(), counts, lastShownKey, random))
    }

    @Test
    fun joiningTipsCatchUpInOneShowThenMixIn() {
        val old = tips("old1", "old2", "old3")
        val joining = tips("new1", "new2", "new3")
        old.forEach { counts[it.id] = 5 }

        val drawn = draw(old + joining, times = 9)

        val expected = mapOf("old1" to 1, "old2" to 1, "old3" to 1, "new1" to 2, "new2" to 2, "new3" to 2)
        assertEquals(setOf("new1", "new2", "new3"), drawn.take(3).toSet())
        assertEquals(expected, drawn.groupingBy { it }.eachCount())
    }

    @Test
    fun joiningTipsMixInWithTheTipsStillUnseenThisCycle() {
        val (seen1, seen2, unseen1, unseen2) = tips("seen1", "seen2", "unseen1", "unseen2")
        val joining = tips("new1", "new2")
        counts[seen1.id] = 5
        counts[seen2.id] = 5
        counts[unseen1.id] = 4
        counts[unseen2.id] = 4

        val drawn = draw(listOf(seen1, seen2, unseen1, unseen2) + joining, times = 4)

        assertEquals(setOf("unseen1", "unseen2", "new1", "new2"), drawn.toSet())
    }

    @Test
    fun fallsBackToAHigherCountWhenTheOnlyLeastShownTipWasLastShown() {
        val (a, b) = tips("a", "b")
        counts[a.id] = 3
        counts[b.id] = 7

        repeat(20) { seed ->
            val pick = pickNext(listOf(a, b), counts, a.id, Random(seed))!!

            assertEquals("b", pick.tip.summary)
            assertEquals(8, pick.countToStore)
        }
    }

    private fun draw(pool: List<VimTip>, times: Int): List<String> {
        return (1..times).map {
            val pick = pickNext(pool, counts, lastShownKey, random)!!
            counts[pick.tip.id] = pick.countToStore
            lastShownKey = pick.tip.id
            pick.tip.summary
        }
    }

    private fun tips(vararg summaries: String): List<VimTip> {
        return summaries.map { vimTip(it, listOf("$it-details")) }
    }
}
