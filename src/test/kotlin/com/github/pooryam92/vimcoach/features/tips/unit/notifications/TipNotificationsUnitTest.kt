package com.github.pooryam92.vimcoach.features.tips.unit.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipActions
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipMessageHandle
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifications
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifier
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TipNotificationsUnitTest {

    private val notifier = FakeTipNotifier()

    private fun controller(
        repository: FakeVimTipRepository = FakeVimTipRepository(),
        settings: FakeSettingsService = FakeSettingsService(),
        ideaVimRcAction: (VimTip) -> (() -> Unit)? = { null },
        ideaVimAvailable: () -> Boolean = { true },
    ) = TipNotifications(
        notifier = notifier,
        tipRepository = { repository },
        settingsRepository = { settings },
        ideaVimRcAction = ideaVimRcAction,
        ideaVimAvailable = ideaVimAvailable,
        openSettings = {},
    )

    @Test
    fun showRandomTipSelectsWithinEnabledCategoriesWhenCategoriesExist() {
        val tip = VimTip("editing tip", listOf("details"), listOf("editing"))
        val repository = FakeVimTipRepository(initialTips = listOf(tip))

        controller(repository, FakeSettingsService(enabledCategories = listOf("editing"))).showRandomTip()

        assertEquals(1, repository.getRandomTipByCategoryCalls)
        assertEquals(listOf("editing"), repository.lastRequestedCategories)
        assertEquals(listOf(tip), notifier.shownTips)
    }

    @Test
    fun showRandomTipUsesUncategorizedSelectionWhenNoCategoriesExist() {
        val tip = VimTip("plain tip", listOf("details"))
        val repository = FakeVimTipRepository(initialTips = listOf(tip))

        controller(repository).showRandomTip()

        assertEquals(1, repository.getRandomTipCalls)
        assertEquals(0, repository.getRandomTipByCategoryCalls)
    }

    @Test
    fun showRandomTipWiresAddToIdeaVimRcActionFromProvider() {
        val action = {}
        controller(ideaVimRcAction = { action }).showRandomTip()

        assertSame(action, notifier.lastActions!!.onAddToIdeaVimRc)
    }

    @Test
    fun showRandomTipExcludesConfigTipsWhenIdeaVimUnavailable() {
        val repository = FakeVimTipRepository(initialTips = listOf(VimTip("plain tip", listOf("details"))))

        controller(repository, ideaVimAvailable = { false }).showRandomTip()

        assertEquals(false, repository.lastIncludeConfigTips)
    }

    @Test
    fun showRandomTipIncludesConfigTipsWhenIdeaVimAvailable() {
        val repository = FakeVimTipRepository(initialTips = listOf(VimTip("plain tip", listOf("details"))))

        controller(repository, ideaVimAvailable = { true }).showRandomTip()

        assertEquals(true, repository.lastIncludeConfigTips)
    }

    @Test
    fun showRandomTipIfNoneActiveShowsWhenNoTipVisible() {
        notifier.visibleTip = false

        val shown = controller().showRandomTipIfNoneActive()

        assertTrue(shown)
        assertEquals(1, notifier.shownTips.size)
    }

    @Test
    fun showRandomTipIfNoneActiveDoesNothingWhenTipVisible() {
        notifier.visibleTip = true
        val repository = FakeVimTipRepository()

        val shown = controller(repository).showRandomTipIfNoneActive()

        assertFalse(shown)
        assertTrue(notifier.shownTips.isEmpty())
        assertEquals(0, repository.getRandomTipCalls)
    }

    @Test
    fun excludeActionHidesTipWithoutShowingAnotherTip() {
        val tip = VimTip("tip", listOf("details"))
        val repository = FakeVimTipRepository(initialTips = listOf(tip))
        val settings = FakeSettingsService()
        controller(repository, settings).showRandomTip()

        notifier.lastActions!!.onExcludeTip()

        assertEquals(listOf(TipHash.fromTip(tip).value), settings.getHiddenTipHashes())
        assertEquals(1, notifier.shownTips.size)
    }

    @Test
    fun excludeActionShowsManagementHintOnlyWhenSettingsRequestIt() {
        val withHint = FakeSettingsService(managementHint = true)
        controller(settings = withHint).showRandomTip()
        notifier.lastActions!!.onExcludeTip()
        assertEquals(1, notifier.tipExcludedShown)

        val noHint = FakeSettingsService(managementHint = false)
        controller(settings = noHint).showRandomTip()
        notifier.lastActions!!.onExcludeTip()
        assertEquals("no extra management hint shown", 1, notifier.tipExcludedShown)
    }

    private class FakeTipNotifier : TipNotifier {
        var visibleTip = false
        val shownTips = mutableListOf<VimTip>()
        var lastActions: TipActions? = null
        var tipExcludedShown = 0

        override fun hasVisibleTip(): Boolean = visibleTip
        override fun showTip(tip: VimTip, actions: TipActions) {
            shownTips.add(tip)
            lastActions = actions
        }
        override fun showTipExcluded(onManage: () -> Unit) { tipExcludedShown += 1 }
        override fun showAddedToIdeaVimRc(onReload: (() -> Unit)?): TipMessageHandle =
            object : TipMessageHandle { override fun dismiss() = Unit }
        override fun showAlreadyInIdeaVimRc() = Unit
        override fun showCreateIdeaVimRcGuidance() = Unit
        override fun showAddToIdeaVimRcFailed(reason: AddTipToIdeaVimRc.FailureReason) = Unit
        override fun showReloadedIdeaVimRc() = Unit
        override fun showReloadIdeaVimRcFailed() = Unit
    }

    private class FakeSettingsService(
        private val enabledCategories: List<String> = emptyList(),
        private val managementHint: Boolean = false,
    ) : SettingsRepository {
        private val hiddenTipHashes = mutableListOf<String>()

        override fun isShowTipsOnStartupEnabled(): Boolean = true
        override fun setShowTipsOnStartupEnabled(enabled: Boolean) = Unit
        override fun isPeriodicTipsEnabled(): Boolean = false
        override fun setPeriodicTipsEnabled(enabled: Boolean) = Unit
        override fun getTipIntervalHours(): Int = 1
        override fun setTipIntervalHours(hours: Int) = Unit
        override fun getEnabledTipCategories(availableCategories: List<String>): List<String> = enabledCategories
        override fun setEnabledTipCategories(availableCategories: List<String>, enabledCategories: List<String>) = Unit
        override fun getHiddenTipHashes(): List<String> = hiddenTipHashes.toList()
        override fun hideTip(hash: String) { if (hash !in hiddenTipHashes) hiddenTipHashes.add(hash) }
        override fun restoreTip(hash: String) { hiddenTipHashes.remove(hash) }
        override fun consumeExcludedTipsManagementHint(): Boolean = managementHint
    }
}
