package com.github.pooryam92.vimcoach.features.tips.unit.notifications

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.notifications.RecordTipNote
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipActions
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipMessageHandle
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifications
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifier
import com.github.pooryam92.vimcoach.features.tips.application.selection.SelectNextTip
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeSettingsService
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class TipNotificationsUnitTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val notifier = FakeTipNotifier()

    private fun controller(
        repository: FakeVimTipRepository = FakeVimTipRepository(),
        settings: FakeSettingsService = FakeSettingsService(),
        ideaVimRcAction: (VimTip) -> (() -> Unit)? = { null },
        ideaVimAvailable: () -> Boolean = { true },
        recordTipNote: RecordTipNote? = null,
        openSettings: () -> Unit = {},
    ): TipNotifications {
        val selectNextTip = SelectNextTip(repository, settings)
        return TipNotifications(
            notifier = notifier,
            tipRepository = { repository },
            settingsRepository = { settings },
            selectNextTip = { selectNextTip },
            ideaVimRcAction = ideaVimRcAction,
            ideaVimAvailable = ideaVimAvailable,
            openSettings = openSettings,
            recordTipNote = recordTipNote,
        )
    }

    @Test
    fun showRandomTipShowsConfigTipsWhenIdeaVimIsAvailable() {
        val configTip = VimTip("config tip", listOf("details"), config = TipConfig(lines = listOf("set number")))
        val repository = FakeVimTipRepository(initialTips = listOf(configTip))

        controller(repository, ideaVimAvailable = { true }).showRandomTip()

        assertEquals(listOf(configTip), notifier.shownTips)
    }

    @Test
    fun showRandomTipHidesConfigTipsWhenIdeaVimIsUnavailable() {
        val configTip = VimTip("config tip", listOf("details"), config = TipConfig(lines = listOf("set number")))
        val repository = FakeVimTipRepository(initialTips = listOf(configTip))

        controller(repository, ideaVimAvailable = { false }).showRandomTip()

        assertEquals("No tips match the selected categories.", notifier.shownTips.single().summary)
    }

    @Test
    fun showRandomTipWiresAddToIdeaVimRcActionFromProvider() {
        val action = {}
        controller(ideaVimRcAction = { action }).showRandomTip()

        assertSame(action, notifier.lastActions!!.onAddToIdeaVimRc)
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
        assertEquals(0, repository.getTipsCalls)
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

    @Test
    fun doesNotWireRecordNoteWhenNoNoteFileConfigured() {
        controller(recordTipNote = null).showRandomTip()

        assertNull(notifier.lastActions!!.onRecordNote)
    }

    @Test
    fun recordNoteAppendsToConfiguredFileWithTheShownTip() {
        val file = tempFolder.root.toPath().resolve("notes.md")
        val tip = VimTip("editing tip", listOf("details"), listOf("editing"))
        val repository = FakeVimTipRepository(initialTips = listOf(tip))
        controller(
            repository = repository,
            settings = FakeSettingsService(enabledCategories = listOf("editing")),
            recordTipNote = RecordTipNote(file),
        ).showRandomTip()

        notifier.lastActions!!.onRecordNote!!("needs a clearer summary")

        val content = Files.readString(file)
        assertTrue(content.contains("editing tip"))
        assertTrue(content.contains("needs a clearer summary"))
    }

    // The nudge policy itself (threshold, cache/setting gating) is covered by AdvancedTipsNudgeUnitTest;
    // these two only pin the orchestrator's wiring: it forwards a positive verdict to the notifier with
    // the openSettings callback, and honors a negative verdict by staying silent.
    @Test
    fun nudgesAdvancedTipsWithOpenSettingsWhenNudgeIsEligible() {
        val openSettings = {}
        val repository = FakeVimTipRepository(
            initialTips = listOf(VimTip("advanced tip", listOf("details"), advanced = true))
        )
        val controller = controller(
            repository,
            FakeSettingsService(showAdvancedTips = false),
            openSettings = openSettings,
        )

        repeat(5) { controller.showRandomTip() }

        assertEquals(1, notifier.advancedTipsNudgeShown)
        assertSame(openSettings, notifier.lastAdvancedTipsOpenSettings)
    }

    @Test
    fun doesNotNudgeAdvancedTipsWhenNudgeIsIneligible() {
        val repository = FakeVimTipRepository(
            initialTips = listOf(VimTip("advanced tip", listOf("details"), advanced = true))
        )

        repeat(5) { controller(repository, FakeSettingsService(showAdvancedTips = true)).showRandomTip() }

        assertEquals(0, notifier.advancedTipsNudgeShown)
    }

    private class FakeTipNotifier : TipNotifier {
        var visibleTip = false
        val shownTips = mutableListOf<VimTip>()
        var lastActions: TipActions? = null
        var tipExcludedShown = 0
        var advancedTipsNudgeShown = 0
        var lastAdvancedTipsOpenSettings: (() -> Unit)? = null

        override fun hasVisibleTip(): Boolean = visibleTip
        override fun showTip(tip: VimTip, actions: TipActions) {
            shownTips.add(tip)
            lastActions = actions
        }
        override fun showTipExcluded(onManage: () -> Unit) { tipExcludedShown += 1 }
        override fun showAdvancedTipsAvailable(onOpenSettings: () -> Unit) {
            advancedTipsNudgeShown += 1
            lastAdvancedTipsOpenSettings = onOpenSettings
        }
        override fun showAddedToIdeaVimRc(onReload: (() -> Unit)?): TipMessageHandle =
            object : TipMessageHandle { override fun dismiss() = Unit }
        override fun showAlreadyInIdeaVimRc() = Unit
        override fun showCreateIdeaVimRcGuidance() = Unit
        override fun showAddToIdeaVimRcFailed(reason: AddTipToIdeaVimRc.FailureReason) = Unit
        override fun showReloadedIdeaVimRc() = Unit
        override fun showReloadIdeaVimRcFailed() = Unit
    }
}
