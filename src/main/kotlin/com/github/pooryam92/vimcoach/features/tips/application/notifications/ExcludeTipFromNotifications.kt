package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService

internal class ExcludeTipFromNotifications(
    private val settingsService: VimCoachSettingsService
) {
    fun exclude(tip: VimTip): TipExclusionResult {
        settingsService.hideTip(TipHash.fromTip(tip).value)
        return TipExclusionResult(
            shouldShowManagementHint = settingsService.consumeExcludedTipsManagementHint()
        )
    }
}
