package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository

/**
 * In-memory [SettingsRepository] double shared across the tips tests. It holds plain state only —
 * nudge policy (threshold, counting) lives in AdvancedTipsNudge, so there is no algorithm here to
 * keep in sync with production.
 */
class FakeSettingsService(
    private val enabledCategories: List<String> = emptyList(),
    private val managementHint: Boolean = false,
    private val showAdvancedTips: Boolean = false,
) : SettingsRepository {
    private val hiddenTipHashes = mutableListOf<String>()
    private var advancedTipsHintShown = false
    private var tipsShownForAdvancedNudge = 0

    override fun isShowTipsOnStartupEnabled(): Boolean = true
    override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit
    override fun isPeriodicTipsEnabled(): Boolean = false
    override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit
    override fun getTipIntervalHours(): Int = 1
    override fun setTipIntervalHours(hours: Int) = Unit
    override fun getEnabledTipCategories(availableCategories: List<String>): List<String> = enabledCategories
    override fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>) = Unit
    override fun getHiddenTipHashes(): List<String> = hiddenTipHashes.toList()
    override fun hideTip(hash: String) { if (hash !in hiddenTipHashes) hiddenTipHashes.add(hash) }
    override fun restoreTip(hash: String) { hiddenTipHashes.remove(hash) }
    override fun consumeExcludedTipsManagementHint(): Boolean = managementHint
    override fun isShowAdvancedTipsEnabled(): Boolean = showAdvancedTips
    override fun setShowAdvancedTipsEnabled(enabled: Boolean) = Unit

    override fun isAdvancedTipsHintShown(): Boolean = advancedTipsHintShown

    override fun consumeAdvancedTipsHint(): Boolean {
        if (advancedTipsHintShown) return false
        advancedTipsHintShown = true
        return true
    }

    override fun getTipsShownForAdvancedNudge(): Int = tipsShownForAdvancedNudge
    override fun setTipsShownForAdvancedNudge(count: Int) {
        tipsShownForAdvancedNudge = count
    }
}
