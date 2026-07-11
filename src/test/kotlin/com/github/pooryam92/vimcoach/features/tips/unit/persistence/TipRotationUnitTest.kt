package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TipRotationUnitTest {

    private val rotation = TipRotation()

    @Test
    fun drawsEveryTipOnceBeforeAnyRepeat() {
        val pool = tips("a", "b", "c", "d", "e")

        val drawnSummaries = (1..pool.size).map { rotation.selectFrom(pool)!!.summary }

        assertEquals(pool.map { it.summary }.toSet(), drawnSummaries.toSet())
    }

    @Test
    fun startsANewFullCycleAfterThePoolIsExhausted() {
        val pool = tips("a", "b", "c")
        repeat(pool.size) { rotation.selectFrom(pool) }

        val secondCycle = (1..pool.size).map { rotation.selectFrom(pool)!!.summary }

        assertEquals(pool.map { it.summary }.toSet(), secondCycle.toSet())
    }

    @Test
    fun exhaustingOnePoolKeepsAnotherPoolsProgress() {
        val poolA = tips("a1", "a2")
        val poolB = tips("b1", "b2")
        repeat(poolA.size) { rotation.selectFrom(poolA) }
        val firstFromB = rotation.selectFrom(poolB)!!

        rotation.selectFrom(poolA)
        val secondFromB = rotation.selectFrom(poolB)!!

        assertNotEquals(firstFromB.summary, secondFromB.summary)
    }

    @Test
    fun returnsNullForAnEmptyPool() {
        assertNull(rotation.selectFrom(emptyList()))
    }

    private fun tips(vararg summaries: String): List<VimTip> {
        return summaries.map { VimTip(it, listOf("$it-details")) }
    }
}
