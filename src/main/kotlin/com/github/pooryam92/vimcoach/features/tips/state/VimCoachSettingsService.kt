package com.github.pooryam92.vimcoach.features.tips.state

interface VimCoachSettingsService {
    fun isShowTipsOnStartupEnabled(): Boolean
    fun setShowTipsOnStartupEnabled(enabled: Boolean)

    fun isPeriodicTipsEnabled(): Boolean
    fun setPeriodicTipsEnabled(enabled: Boolean)

    fun getTipIntervalHours(): Int
    fun setTipIntervalHours(hours: Int)

    fun getEnabledTipCategories(availableCategories: List<String>): List<String>
    fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>)
}
