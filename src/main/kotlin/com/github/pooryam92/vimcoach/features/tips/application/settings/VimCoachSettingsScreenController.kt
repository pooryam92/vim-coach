package com.github.pooryam92.vimcoach.features.tips.application.settings

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service

class VimCoachSettingsScreenController() {
    private var injectedSettingsService: VimCoachSettingsService? = null
    private var injectedTipService: VimTipService? = null
    private var injectedRefreshTips: RefreshTips? = null

    internal constructor(
        settingsService: VimCoachSettingsService,
        tipService: VimTipService,
        refreshTips: RefreshTips? = null
    ) : this() {
        injectedSettingsService = settingsService
        injectedTipService = tipService
        injectedRefreshTips = refreshTips
    }

    fun loadState(): VimCoachSettingsScreenState {
        val settingsService = settingsService()
        val availableCategories = loadAvailableCategories()

        return VimCoachSettingsScreenState(
            showTipsOnStartup = settingsService.isShowTipsOnStartupEnabled(),
            periodicTipsEnabled = settingsService.isPeriodicTipsEnabled(),
            tipIntervalHours = settingsService.getTipIntervalHours(),
            availableCategories = availableCategories,
            enabledCategories = settingsService.getEnabledTipCategories(availableCategories),
            excludedTips = loadExcludedTips(settingsService.getHiddenTipHashes())
        )
    }

    fun saveState(state: VimCoachSettingsScreenState) {
        val settingsService = settingsService()
        settingsService.setShowTipsOnStartupEnabled(state.showTipsOnStartup)
        settingsService.setTipIntervalHours(state.tipIntervalHours)
        settingsService.setPeriodicTipsEnabled(state.periodicTipsEnabled)
        settingsService.setEnabledTipCategories(state.availableCategories, state.enabledCategories)
        restoreTipsRemovedFromSettings(state.excludedTips)
    }

    private fun loadAvailableCategories(): List<String> {
        val tipService = tipService()
        val categories = tipService.getCategories().values
        if (categories.isNotEmpty() || tipService.countTips() == 0) {
            return categories
        }

        // Legacy caches from pre-category versions need a full reload to recover category data.
        refreshTips().refetchTips()
        return tipService.getCategories().values
    }

    private fun loadExcludedTips(hiddenTipHashes: List<String>): List<ExcludedTipSettingsItem> {
        return tipService().getTipsByHashes(hiddenTipHashes).map { tip ->
            ExcludedTipSettingsItem(
                hash = TipHash.fromTip(tip).value,
                summary = tip.summary
            )
        }
    }

    private fun restoreTipsRemovedFromSettings(excludedTips: List<ExcludedTipSettingsItem>) {
        val settingsService = settingsService()
        val currentExcludedHashes = loadExcludedTips(settingsService.getHiddenTipHashes())
            .map(ExcludedTipSettingsItem::hash)
            .toSet()
        val remainingHashes = excludedTips.map(ExcludedTipSettingsItem::hash).toSet()

        currentExcludedHashes
            .filterNot(remainingHashes::contains)
            .forEach(settingsService::restoreTip)
    }

    private fun settingsService(): VimCoachSettingsService = injectedSettingsService ?: service()

    private fun tipService(): VimTipService = injectedTipService ?: service()

    private fun refreshTips(): RefreshTips = injectedRefreshTips ?: service()
}
