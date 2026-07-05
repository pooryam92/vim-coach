package com.github.pooryam92.vimcoach.features.tips.application.settings

data class VimCoachSettingsScreenState(
    val showTipsOnStartup: Boolean,
    val periodicTipsEnabled: Boolean,
    val tipIntervalHours: Int,
    val availableCategories: List<String>,
    val enabledCategories: List<String>,
    val showAdvancedTips: Boolean = false,
    val excludedTips: List<ExcludedTipSettingsItem> = emptyList(),
    val restoredExcludedTipHashes: List<String> = emptyList()
)

data class ExcludedTipSettingsItem(
    val hash: String,
    val summary: String
)
