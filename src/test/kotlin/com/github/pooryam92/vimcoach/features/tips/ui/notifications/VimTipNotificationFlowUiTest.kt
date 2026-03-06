package com.github.pooryam92.vimcoach.features.tips.ui.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.state.VimTipService
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.VimTipNotifier
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VimTipNotificationFlowUiTest : BasePlatformTestCase() {

    fun testNotificationAddsNextTipAction() {
        val mockTipService = createMockTipService(
            VimTip(summary = "Tip 1", details = listOf("Details 1")),
            VimTip(summary = "Tip 2", details = listOf("Details 2"))
        )
        val notifier = VimTipNotifier(mockTipService)

        val notification = notifier.createNotificationWithActions(project, mockTipService.getRandomTip())

        assertEquals(1, notification.actions.size)
        assertEquals(VimTipNotifier.TIP_NEXT_ACTION_TEXT, notification.actions[0].templateText)
    }

    fun testShowRandomTipRequestsTipFromService() {
        var getRandomTipCalls = 0
        val mockTipService = object : VimTipService {
            override fun countTips() = 1
            override fun saveTips(tips: List<VimTip>) = Unit
            override fun getRandomTip(): VimTip {
                getRandomTipCalls += 1
                return VimTip("Tip", listOf("Details"))
            }
            override fun getMetadata() = TipMetadata()
            override fun saveMetadata(metadata: TipMetadata) = Unit
            override fun getState() = VimTipService.State(mutableListOf())
            override fun loadState(state: VimTipService.State) = Unit
        }
        val notifier = VimTipNotifier(mockTipService)

        notifier.showRandomTip(project)

        assertEquals(1, getRandomTipCalls)
    }

    private fun createMockTipService(vararg tips: VimTip): VimTipService {
        val tipList = tips.toList()
        return object : VimTipService {
            private var currentIndex = 0

            override fun countTips() = tipList.size
            override fun saveTips(tips: List<VimTip>) = Unit
            override fun getRandomTip(): VimTip {
                val tip = tipList[currentIndex % tipList.size]
                currentIndex += 1
                return tip
            }
            override fun getMetadata() = TipMetadata()
            override fun saveMetadata(metadata: TipMetadata) = Unit
            override fun getState() = VimTipService.State(tipList.toMutableList())
            override fun loadState(state: VimTipService.State) = Unit
        }
    }
}
