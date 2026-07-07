package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository

/**
 * Owns the "should we announce advanced tips now?" decision. The opt-in is otherwise passive; once
 * advanced tips exist in the cache and the setting is off, we announce them once so the toggle is
 * discoverable — but held back until the user has seen a few tips (so a fresh install isn't ambushed
 * on its first tip) and then only on the first eligible tip, so it fires exactly once per user.
 */
internal class AdvancedTipsNudge(
    private val settingsService: SettingsRepository,
    private val tipRepository: VimTipRepository,
) {
    // Guards short-circuit before either one-time flag is touched: the counter only advances while a
    // nudge is genuinely pending, and the hint is consumed only on the eligible tip. Order matters —
    // record before consume — so keeping both steps here removes that sequencing hazard from callers.
    // The hint flag is checked first (read-only): once it is set — nudge fired, or the user found the
    // toggle themselves — every later tip exits on a single boolean read instead of scanning the cache.
    fun shouldNudgeAfterTipShown(): Boolean {
        if (settingsService.isAdvancedTipsHintShown()) return false
        if (settingsService.isShowAdvancedTipsEnabled()) return false
        if (!tipRepository.hasAdvancedTips()) return false
        if (!recordTipShownAndCheckReady()) return false
        return settingsService.consumeAdvancedTipsHint()
    }

    // Self-bounding: stops writing once the threshold is reached, so a pending nudge costs at most
    // three settings writes.
    private fun recordTipShownAndCheckReady(): Boolean {
        val count = settingsService.getTipsShownForAdvancedNudge()
        if (count >= ADVANCED_NUDGE_TIP_THRESHOLD) {
            return true
        }

        val updated = count + 1
        settingsService.setTipsShownForAdvancedNudge(updated)
        return updated >= ADVANCED_NUDGE_TIP_THRESHOLD
    }

    private companion object {
        // The nudge fires on the 3rd tip a user sees while it's pending, not the first, so a fresh
        // install isn't ambushed with a settings pointer on its very first tip.
        const val ADVANCED_NUDGE_TIP_THRESHOLD = 3
    }
}
