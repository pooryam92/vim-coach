package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

/**
 * No-repeat draw: every tip in a pool is shown once before any repeats. Deliberately in-memory —
 * the cycle restarts with the IDE. On exhaustion only the exhausted pool's hashes are forgotten,
 * so cycling through one category filter never resets progress through another.
 */
internal class TipRotation {

    private val shownTipHashes = mutableSetOf<String>()

    @Synchronized
    fun selectFrom(pool: List<VimTip>): VimTip? {
        if (pool.isEmpty()) return null

        val unshownTips = pool.filterNot { TipHash.fromTip(it).value in shownTipHashes }
        val candidates = unshownTips.ifEmpty {
            shownTipHashes.removeAll(pool.map { TipHash.fromTip(it).value }.toSet())
            pool
        }

        val selectedTip = candidates.random()
        shownTipHashes.add(TipHash.fromTip(selectedTip).value)
        return selectedTip
    }
}
