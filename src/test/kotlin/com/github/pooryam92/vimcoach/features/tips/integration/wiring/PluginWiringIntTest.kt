package com.github.pooryam92.vimcoach.features.tips.integration.wiring

import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.application.PeriodicTipSchedulerService
import com.github.pooryam92.vimcoach.features.tips.application.TipNotificationService
import com.github.pooryam92.vimcoach.features.tips.application.VimCoachSettingsScreenService
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimCoachSettingsStore
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PluginWiringIntTest : BasePlatformTestCase() {

    fun testProjectServicesAreRegistered() {
        val periodicSchedulerService = project.service<PeriodicTipSchedulerService>()
        val tipNotificationService = project.service<TipNotificationService>()

        assertNotNull(periodicSchedulerService)
        assertNotNull(tipNotificationService)
    }

    fun testApplicationServicesAreRegistered() {
        val tipStore = service<VimTipStore>()
        val settingsStore = service<VimCoachSettingsStore>()
        val tipService = service<VimTipService>()
        val settingsService = service<VimCoachSettingsService>()
        val sourceService = service<TipSourceService>()
        val loaderService = service<TipLoaderService>()
        val settingsScreenService = service<VimCoachSettingsScreenService>()

        assertNotNull(tipStore)
        assertNotNull(settingsStore)
        assertNotNull(tipService)
        assertNotNull(settingsService)
        assertNotNull(sourceService)
        assertNotNull(loaderService)
        assertNotNull(settingsScreenService)
    }
}
