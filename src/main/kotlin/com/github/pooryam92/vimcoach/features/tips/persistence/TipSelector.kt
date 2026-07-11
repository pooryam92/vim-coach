package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

internal data class TipVisibilityCriteria(
    val hiddenTipHashes: Set<String>,
    val includeConfigTips: Boolean,
    val showAdvancedTips: Boolean
)

/**
 * Single chokepoint for every filter applied before a tip is shown: eligibility filters first,
 * then the no-repeat rotation over whatever survived them. Returns null when nothing is eligible.
 */
internal class TipSelector(private val rotation: TipRotation = TipRotation()) {

    fun select(tips: List<VimTip>, criteria: TipVisibilityCriteria): VimTip? {
        return rotation.selectFrom(eligibleTips(tips, criteria))
    }

    private fun eligibleTips(tips: List<VimTip>, criteria: TipVisibilityCriteria): List<VimTip> {
        return tips
            .asSequence()
            .filterNot { TipHash.fromTip(it).value in criteria.hiddenTipHashes }
            .filter { criteria.includeConfigTips || it.config?.lines.isNullOrEmpty() }
            .filter { criteria.showAdvancedTips || !it.advanced }
            .toList()
    }
}
