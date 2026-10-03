package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.RotationProgress
import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotationRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentTipRotationStore
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.concurrent.thread

class TipRotationRepositoryUnitTest {

    private val store = PersistentTipRotationStore()
    private val allKeys = setOf("a", "b")

    @Test
    fun freshStoreHasNoProgress() {
        val repository = TipRotationRepositoryImpl(store)

        assertEquals(RotationProgress(emptyMap(), lastShownKey = null), repository.getProgress())
    }

    @Test
    fun aSecondRepositoryOverTheSameStoreSeesWhatTheFirstWrote() {
        TipRotationRepositoryImpl(store).recordShown("a", 3, allKeys)

        val second = TipRotationRepositoryImpl(store)

        assertEquals(RotationProgress(mapOf("a" to 3), "a"), second.getProgress())
    }

    @Test
    fun recordShownOverwritesTheCountAndLastShownKey() {
        val repository = TipRotationRepositoryImpl(store)
        repository.recordShown("a", 1, allKeys)
        repository.recordShown("b", 1, allKeys)

        repository.recordShown("a", 2, allKeys)

        assertEquals(RotationProgress(mapOf("a" to 2, "b" to 1), "a"), repository.getProgress())
    }

    @Test
    fun recordShownDropsKeysMissingFromTheCache() {
        val repository = TipRotationRepositoryImpl(store)
        repository.recordShown("a", 1, allKeys)
        repository.recordShown("b", 1, allKeys)

        repository.recordShown("a", 2, cachedKeys = setOf("a"))

        assertEquals(mapOf("a" to 2), repository.getProgress().timesShown)
    }

    @Test
    fun concurrentShowsKeepEveryCount() {
        val repository = TipRotationRepositoryImpl(store)
        val keys = (1..2000).map { "k$it" }
        val cachedKeys = keys.toSet()

        keys.chunked(250)
            .map { chunk -> thread { chunk.forEach { repository.recordShown(it, 1, cachedKeys) } } }
            .forEach(Thread::join)

        assertEquals(cachedKeys, repository.getProgress().timesShown.keys)
    }
}
