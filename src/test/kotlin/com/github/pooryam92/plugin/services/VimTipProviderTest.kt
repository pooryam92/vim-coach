package com.github.pooryam92.plugin.services

import com.github.pooryam92.vimcoach.services.VimTip
import com.github.pooryam92.vimcoach.services.VimTipProvider
import com.github.pooryam92.vimcoach.services.VimTips
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipProviderTest : BasePlatformTestCase() {

    fun testReturnsSavedTip() {
        val vimTips = project.service<VimTips>()
        val tip = VimTip("summary", "details", "category")
        vimTips.saveTips(listOf(tip))

        val provider = project.service<VimTipProvider>()

        assertEquals(tip, provider.getRandomTip())
    }
}
