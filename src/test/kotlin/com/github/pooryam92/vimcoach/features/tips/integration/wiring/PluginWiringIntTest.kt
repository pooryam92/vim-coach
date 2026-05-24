package com.github.pooryam92.vimcoach.features.tips.integration.wiring

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.scheduling.ScheduleTips
import com.github.pooryam92.vimcoach.features.tips.application.settings.VimCoachSettingsScreenController
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
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
        val tipStore = service<VimTipStore>()
        val settingsStore = service<VimCoachSettingsStore>()
        val tipService = service<VimTipService>()
        val settingsService = service<VimCoachSettingsService>()
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
