package com.github.pooryam92.vimcoach.services

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipServiceTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        project.service<VimTipService>().saveTips(emptyList())
    }

    fun testCountTipsAfterSave() {
        // Arrange
        val service = project.service<VimTipService>()
        val tips = listOf(
            VimTip("summary-1", "details-1", "category-1", "normal"),
            VimTip("summary-2", "details-2", "category-2", "visual")
        )
        service.saveTips(tips)

        // Act
        val count = service.countTips()

        // Assert
        assertEquals(2, count)
    }

    fun testGetRandomTipReturnsSavedTip() {
        // Arrange
        val service = project.service<VimTipService>()
        val tips = listOf(
            VimTip("summary-1", "details-1", "category-1", "normal"),
            VimTip("summary-2", "details-2", "category-2", "visual")
        )
        service.saveTips(tips)

        // Act
        val randomTip = service.getRandomTip()

        // Assert
        assertTrue(tips.contains(randomTip))
    }

    fun testLoadStateReplacesTips() {
        // Arrange
        val service = project.service<VimTipService>()
        service.saveTips(listOf(VimTip("old-summary", "old-details", "category", "normal")))

        val newTip = VimTip("new-summary", "new-details", null, null)

        // Act
        service.loadState(VimTipService.State(mutableListOf(newTip)))

        // Assert
        assertEquals(1, service.countTips())
    }


    fun testGetRandomTipReturnsEmptyMessageWhenEmpty() {
        // Arrange
        val service = project.service<VimTipService>()

        // Act
        val randomTip = service.getRandomTip()

        // Assert
        assertEquals("Tips have not been loaded yet.", randomTip.details)
    }
}
