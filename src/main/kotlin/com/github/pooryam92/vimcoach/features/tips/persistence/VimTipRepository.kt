package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

interface VimTipRepository {
    fun countTips(): Int

    fun saveTips(tips: List<VimTip>)

    /**
     * [includeConfigTips] = false drops tips carrying an .ideavimrc snippet ([VimTip.config]).
     * Such tips are only actionable when IdeaVim is installed (their "Add to .ideavimrc" button
     * needs it), so callers without IdeaVim exclude them rather than show an inapplicable tip.
     */
    fun getRandomTip(includeConfigTips: Boolean = true): VimTip

    fun getRandomTip(categories: List<String>, includeConfigTips: Boolean = true): VimTip

    fun getTipsByHashes(hashes: List<String>): List<VimTip>

    /**
     * Returns the persisted category cache.
     * If tips exist but cached categories are empty, implementations should
     * rebuild the categories from the stored tips and persist the result.
     */
    fun getCategories(): TipCategories

    fun getMetadata(): TipMetadata

    fun saveMetadata(metadata: TipMetadata)
}
