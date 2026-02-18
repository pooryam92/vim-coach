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
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
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
            VimTip("summary-1", listOf("details-1")),
            VimTip("summary-2", listOf("details-2"))
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
        service.saveTips(listOf(VimTip("old-summary", listOf("old-details"))))

        val newTip = VimTip("new-summary", listOf("new-details"))

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
        assertEquals("Tips have not been loaded yet.", randomTip.details.single())
    }
}
