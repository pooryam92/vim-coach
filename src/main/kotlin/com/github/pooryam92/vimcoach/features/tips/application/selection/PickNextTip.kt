package com.github.pooryam92.vimcoach.features.tips.application.selection

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import kotlin.random.Random

internal data class TipPick(val tip: VimTip, val countToStore: Int)

internal fun pickNext(
    pool: List<VimTip>,
    counts: Map<String, Int>,
    lastShownKey: String?,
    random: Random
): TipPick? {
    if (pool.isEmpty()) return null

    val tipsByKey = pool.associateBy(VimTip::id)
    val effectiveCounts = capDeficit(tipsByKey.keys.associateWith { counts[it] ?: 0 })
    val key = candidates(effectiveCounts, lastShownKey).random(random)
    return TipPick(tipsByKey.getValue(key), effectiveCounts.getValue(key) + 1)
}

private fun capDeficit(counts: Map<String, Int>): Map<String, Int> {
    val floor = counts.values.max() - 1
    return counts.mapValues { (_, count) -> maxOf(count, floor) }
}

private fun candidates(counts: Map<String, Int>, lastShownKey: String?): Set<String> {
    val notLastShown = counts.keys - setOfNotNull(lastShownKey)
    return (leastShown(counts) intersect notLastShown)
        .ifEmpty { notLastShown }
        .ifEmpty { counts.keys }
}

private fun leastShown(counts: Map<String, Int>): Set<String> {
    val lowest = counts.values.min()
    return counts.filterValues { it == lowest }.keys
}
