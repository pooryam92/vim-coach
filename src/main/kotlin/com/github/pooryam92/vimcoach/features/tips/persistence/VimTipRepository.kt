package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

interface VimTipRepository {
    fun countTips(): Int

    fun saveTips(tips: List<VimTip>)

    fun getTips(): List<VimTip>

    fun getTipsByHashes(hashes: List<String>): List<VimTip>

    /** Whether the cache holds any tip marked [VimTip.advanced]; drives the one-time opt-in nudge. */
    fun hasAdvancedTips(): Boolean

    /**
     * Returns the persisted category cache.
     * If tips exist but cached categories are empty, implementations should
     * rebuild the categories from the stored tips and persist the result.
     */
    fun getCategories(): TipCategories

    fun getMetadata(): TipMetadata

    fun saveMetadata(metadata: TipMetadata)
}
