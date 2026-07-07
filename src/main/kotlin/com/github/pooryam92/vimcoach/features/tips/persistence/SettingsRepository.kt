package com.github.pooryam92.vimcoach.features.tips.persistence

interface SettingsRepository {
    fun isShowTipsOnStartupEnabled(): Boolean
    fun setShowTipsOnStartupEnabled(enabled: Boolean)

    fun isPeriodicTipsEnabled(): Boolean
    fun setPeriodicTipsEnabled(enabled: Boolean)

    fun getTipIntervalHours(): Int
    fun setTipIntervalHours(hours: Int)

    fun getEnabledTipCategories(availableCategories: List<String>): List<String>
    fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>)

    fun getHiddenTipHashes(): List<String>
    fun hideTip(hash: String)
    fun restoreTip(hash: String)
    fun consumeExcludedTipsManagementHint(): Boolean

    fun isShowAdvancedTipsEnabled(): Boolean
    fun setShowAdvancedTipsEnabled(enabled: Boolean)

    /** Read-only view of the one-shot advanced-tips hint, so guards can check without spending it. */
    fun isAdvancedTipsHintShown(): Boolean

    /** Returns true exactly once — the first time the advanced-tips nudge should be shown. */
    fun consumeAdvancedTipsHint(): Boolean

    /**
     * Persisted count of tips shown while the advanced-tips nudge is pending. Plain storage; the
     * nudge policy (threshold, when to advance) lives in AdvancedTipsNudge.
     */
    fun getTipsShownForAdvancedNudge(): Int
    fun setTipsShownForAdvancedNudge(count: Int)
}
