package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

interface VimTipService {
    fun countTips(): Int

    fun saveTips(tips: List<VimTip>)

    fun getRandomTip(): VimTip

    /**
     * Returns the persisted category cache.
     * If tips exist but cached categories are empty, implementations should
     * rebuild the categories from the stored tips and persist the result.
     */
    fun getCategories(): TipCategories

    fun getMetadata(): TipMetadata

    fun saveMetadata(metadata: TipMetadata)
}
