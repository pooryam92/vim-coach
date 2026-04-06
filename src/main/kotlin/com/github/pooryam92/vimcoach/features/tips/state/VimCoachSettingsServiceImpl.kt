package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.application.PeriodicTipSchedulerService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager

class VimCoachSettingsServiceImpl() : VimCoachSettingsService {
    private var injectedSettingsStore: VimCoachSettingsStore? = null

    internal constructor(settingsStore: VimCoachSettingsStore) : this() {
        injectedSettingsStore = settingsStore
    }

    override fun isShowTipsOnStartupEnabled(): Boolean {
        return currentState().showTipsOnStartup
    }

    override fun setShowTipsOnStartupEnabled(enabled: Boolean) {
        settingsStore().setShowTipsOnStartup(enabled)
    }

    override fun isPeriodicTipsEnabled(): Boolean {
        return currentState().periodicTipsEnabled
    }

    override fun setPeriodicTipsEnabled(enabled: Boolean) {
        if (currentState().periodicTipsEnabled == enabled) {
            return
        }
        settingsStore().setPeriodicTipsEnabled(enabled)
        notifyPeriodicSchedulerSettingsChanged()
    }

    override fun getTipIntervalHours(): Int {
        return currentState().tipIntervalHours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS)
    }

    override fun setTipIntervalHours(hours: Int) {
        val normalizedHours = hours.coerceAtLeast(MIN_TIP_INTERVAL_HOURS)
        val previousState = currentState()
        if (previousState.tipIntervalHours == normalizedHours) {
            return
        }

        settingsStore().setTipIntervalHours(normalizedHours)
        if (previousState.periodicTipsEnabled) {
            notifyPeriodicSchedulerSettingsChanged()
        }
    }

    override fun getEnabledTipCategories(availableCategories: List<String>): List<String> {
        val normalizedAvailable = normalizeCategoryNames(availableCategories)
        val disabledCategories = disabledCategorySet()

        return normalizedAvailable
            .asSequence()
            .filterNot(disabledCategories::contains)
            .toList()
    }

    override fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>) {
        val normalizedDisabled = disabledCategoriesFor(availableCategories, enabledCategories)

        if (disabledCategoryList() == normalizedDisabled) {
            return
        }

        settingsStore().setDisabledTipCategories(normalizedDisabled)
    }

    private fun currentState(): VimCoachSettingsStore.State {
        return settingsStore().state ?: VimCoachSettingsStore.State()
    }

    private fun settingsStore(): VimCoachSettingsStore {
        return injectedSettingsStore ?: service()
    }

    private fun disabledCategoriesFor(
        availableCategories: List<String>,
        enabledCategories: List<String>
    ): List<String> {
        val normalizedAvailable = normalizeCategoryNames(availableCategories)
        val normalizedEnabled = normalizeCategoryNames(enabledCategories).toSet()
        return normalizedAvailable.filterNot(normalizedEnabled::contains)
    }

    private fun disabledCategoryList(): List<String> {
        return normalizeCategoryNames(currentState().disabledTipCategories)
    }

    private fun disabledCategorySet(): Set<String> {
        return disabledCategoryList().toSet()
    }

    private fun normalizeCategoryNames(categories: List<String>): List<String> {
        return categories
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .toList()
    }

    private companion object {
        const val MIN_TIP_INTERVAL_HOURS = 1
    }

    private fun notifyPeriodicSchedulerSettingsChanged() {
        val app = ApplicationManager.getApplication() ?: return
        if (app.isDisposed) {
            return
        }

        ProjectManager.getInstance().openProjects
            .filter { !it.isDisposed }
            .forEach { project ->
                project.service<PeriodicTipSchedulerService>().onSettingsChanged()
            }
    }
}
