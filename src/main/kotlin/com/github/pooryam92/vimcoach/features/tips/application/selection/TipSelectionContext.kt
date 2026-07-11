package com.github.pooryam92.vimcoach.features.tips.application.selection

/**
 * Inputs to the [TipFilter] chain, read from [com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository]
 * once per [SelectNextTip.select] call. [availableCategories] is kept separate from [enabledCategories] so
 * [categoryFilter] can tell "no categories exist yet" (pass everything through) apart from "categories exist
 * but the user enabled none of them" (filter everything out).
 */
internal data class TipSelectionContext(
    val availableCategories: List<String>,
    val enabledCategories: List<String>,
    val hiddenTipHashes: Set<String>,
    val showAdvancedTips: Boolean,
    val includeConfigTips: Boolean,
)
