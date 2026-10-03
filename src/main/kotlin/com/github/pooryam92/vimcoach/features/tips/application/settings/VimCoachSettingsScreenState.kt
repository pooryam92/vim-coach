package com.github.pooryam92.vimcoach.features.tips.application.settings

data class VimCoachSettingsScreenState(
    val showTipsOnStartup: Boolean,
    val periodicTipsEnabled: Boolean,
    val tipIntervalHours: Int,
    val availableCategories: List<String>,
    val enabledCategories: List<String>,
    val showAdvancedTips: Boolean = false,
    val excludedTips: List<ExcludedTipSettingsItem> = emptyList(),
    val restoredExcludedTipIds: List<String> = emptyList()
)

data class ExcludedTipSettingsItem(
    val id: String,
    val summary: String
)
