package com.github.pooryam92.vimcoach.features.tips.persistence

/** Per-machine rotation progress, keyed by [com.github.pooryam92.vimcoach.features.tips.domain.TipHash.fromContent]. */
interface TipRotationRepository {
    fun getProgress(): RotationProgress

    /** Records one show and, in the same write, forgets keys missing from [cachedKeys]. */
    fun recordShown(key: String, count: Int, cachedKeys: Set<String>)
}

data class RotationProgress(val timesShown: Map<String, Int>, val lastShownKey: String?)
