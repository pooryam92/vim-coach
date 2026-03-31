package com.github.pooryam92.vimcoach.features.tips.application

data class VimCoachSettingsScreenState(
    val showTipsOnStartup: Boolean,
    val periodicTipsEnabled: Boolean,
    val tipIntervalHours: Int,
    val availableCategories: List<String>,
    val enabledCategories: List<String>
)
