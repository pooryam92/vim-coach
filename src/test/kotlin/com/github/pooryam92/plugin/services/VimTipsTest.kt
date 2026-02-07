package com.github.pooryam92.plugin.services

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipsTest : BasePlatformTestCase() {

    fun testPlaceholderAndSave() {
        val vimTips = project.service<VimTips>()
        vimTips.saveTips(emptyList())

        val placeholder = vimTips.getTips()
        assertEquals(1, placeholder.size)
        assertEquals("No tips found.", placeholder[0].summary)
        assertEquals("Tips have not been loaded yet.", placeholder[0].details)
        assertEquals(null, placeholder[0].category)

        val saved = listOf(VimTip("summary", "details", "category"))
        vimTips.saveTips(saved)
        assertEquals(1, vimTips.countTips())
        assertEquals(saved, vimTips.getTips())
    }
}
