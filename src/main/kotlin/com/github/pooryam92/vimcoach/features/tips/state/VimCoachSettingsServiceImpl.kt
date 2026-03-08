package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.application.PeriodicTipSchedulerService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager

class VimCoachSettingsServiceImpl(
    private val settingsStore: VimCoachSettingsStore = service()
) : VimCoachSettingsService {

    override fun isShowTipsOnStartupEnabled(): Boolean {
        return currentState().showTipsOnStartup
    }

    override fun setShowTipsOnStartupEnabled(enabled: Boolean) {
        settingsStore.setShowTipsOnStartup(enabled)
    }

    override fun isPeriodicTipsEnabled(): Boolean {
        return currentState().periodicTipsEnabled
    }

    override fun setPeriodicTipsEnabled(enabled: Boolean) {
        if (currentState().periodicTipsEnabled == enabled) {
            return
        }
        settingsStore.setPeriodicTipsEnabled(enabled)
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

        settingsStore.setTipIntervalHours(normalizedHours)
        if (previousState.periodicTipsEnabled) {
            notifyPeriodicSchedulerSettingsChanged()
        }
    }

    private fun currentState(): VimCoachSettingsStore.State {
        return settingsStore.state ?: VimCoachSettingsStore.State()
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
