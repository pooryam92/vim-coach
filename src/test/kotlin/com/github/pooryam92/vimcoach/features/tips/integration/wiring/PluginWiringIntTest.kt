package com.github.pooryam92.vimcoach.features.tips.integration.wiring

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.scheduling.ScheduleTips
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenController
import com.github.pooryam92.vimcoach.features.tips.application.loading.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PluginWiringIntTest : BasePlatformTestCase() {

    fun testProjectServicesAreRegistered() {
        val scheduleTips = project.service<ScheduleTips>()
        val showTips = project.service<ShowTips>()

        assertNotNull(scheduleTips)
        assertNotNull(showTips)
    }

    fun testApplicationServicesAreRegistered() {
        val tipStore = service<PersistentVimTipStore>()
        val settingsStore = service<PersistentSettingsStore>()
        val tipService = service<VimTipRepository>()
        val settingsService = service<SettingsRepository>()
        val sourceService = service<TipSourceService>()
        val refreshTips = service<RefreshTips>()
        val settingsScreenService = service<VimCoachSettingsScreenController>()

        assertNotNull(tipStore)
        assertNotNull(settingsStore)
        assertNotNull(tipService)
        assertNotNull(settingsService)
        assertNotNull(sourceService)
        assertNotNull(refreshTips)
        assertNotNull(settingsScreenService)
    }
}
