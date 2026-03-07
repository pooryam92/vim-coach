package com.github.pooryam92.vimcoach.features.tips.integration.wiring

import com.github.pooryam92.vimcoach.features.tips.application.TipLoaderService
import com.github.pooryam92.vimcoach.features.tips.source.application.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.state.VimCoachSettingsService
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PluginWiringIntTest : BasePlatformTestCase() {

    fun testProjectServicesAreRegistered() {
        val sourceService = project.service<TipSourceService>()
        val loaderService = project.service<TipLoaderService>()

        assertNotNull(sourceService)
        assertNotNull(loaderService)
    }

    fun testApplicationServicesAreRegistered() {
        val tipService = service<VimTipService>()
        val settingsService = service<VimCoachSettingsService>()

        assertNotNull(tipService)
        assertNotNull(settingsService)
    }
}
