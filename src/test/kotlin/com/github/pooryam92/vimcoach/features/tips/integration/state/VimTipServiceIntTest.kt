package com.github.pooryam92.vimcoach.features.tips.integration.state

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipServiceIntTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        service().saveTips(emptyList())
    }

    fun testCountTipsAfterSave() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        service().saveTips(tips)

        assertEquals(2, service().countTips())
    }

    fun testGetRandomTipReturnsSavedTip() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        service().saveTips(tips)

        assertTrue(tips.contains(service().getRandomTip()))
    }

    fun testLoadStateReplacesTips() {
        val service = service()
        service.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        service.loadState(VimTipService.State(mutableListOf(VimTip("new-summary", listOf("new-details")))))

        assertEquals(1, service.countTips())
    }

    fun testGetRandomTipReturnsEmptyMessageWhenEmpty() {
        val randomTip = service().getRandomTip()

        assertEquals("No tips found.", randomTip.summary)
        assertEquals("Tips have not been loaded yet.", randomTip.details.single())
    }

    private fun service(): VimTipService = project.service()
}
