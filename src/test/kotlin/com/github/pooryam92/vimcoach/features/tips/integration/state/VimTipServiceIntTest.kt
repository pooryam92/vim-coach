package com.github.pooryam92.vimcoach.features.tips.integration.state

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.state.store.VimTipStore
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipServiceIntTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        tipService().saveTips(emptyList())
    }

    fun testCountTipsAfterSave() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertEquals(2, tipService().countTips())
    }

    fun testGetRandomTipReturnsSavedTip() {
        val tips = listOf(
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
        )
        tipService().saveTips(tips)

        assertTrue(tips.contains(tipService().getRandomTip()))
    }

    fun testLoadStateReplacesTips() {
        val service = tipService()
        service.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        tipStore().loadState(VimTipStore.State(mutableListOf(VimTip("new-summary", listOf("new-details")))))

        assertEquals(1, service.countTips())
    }

    fun testGetRandomTipReturnsEmptyMessageWhenEmpty() {
        val randomTip = tipService().getRandomTip()

        assertEquals("No tips found.", randomTip.summary)
        assertEquals("Tips have not been loaded yet.", randomTip.details.single())
    }

    private fun tipService(): VimTipService = service()

    private fun tipStore(): VimTipStore = service()
}
