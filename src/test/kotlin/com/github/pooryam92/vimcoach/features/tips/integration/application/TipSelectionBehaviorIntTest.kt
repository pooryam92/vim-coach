package com.github.pooryam92.vimcoach.features.tips.integration.application

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.AddTipToIdeaVimRc
import com.github.pooryam92.vimcoach.features.tips.application.notifications.ShowTips
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipActions
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipMessageHandle
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifications
import com.github.pooryam92.vimcoach.features.tips.application.notifications.TipNotifier
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.ui.notifications.IntelliJTipNotifier
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

/**
 * Pins tip-selection behavior at the seam that survives the selection redesign
 * (docs/discover/tip-selection-redesign.md): production `ShowTips` wiring in front, the
 * `TipNotifier` port behind. Nothing here references where the filters or the rotation are
 * implemented, so this suite must pass unchanged before, during, and after the migration.
 */
class TipSelectionBehaviorIntTest : BasePlatformTestCase() {

    private lateinit var settings: SettingsRepository
    private lateinit var tipRepository: VimTipRepository
    private lateinit var notifier: RecordingTipNotifier
    private lateinit var showTips: ShowTips

    override fun setUp() {
        super.setUp()
        settings = SettingsRepositoryImpl(PersistentSettingsStore())
        tipRepository = VimTipRepositoryImpl(PersistentVimTipStore())
        ApplicationManager.getApplication().registerServiceInstance(SettingsRepository::class.java, settings)
        ApplicationManager.getApplication().registerServiceInstance(VimTipRepository::class.java, tipRepository)
        notifier = RecordingTipNotifier()
        project.registerServiceInstance(TipNotifier::class.java, notifier)
        showTips = TipNotifications(project)
    }

    override fun tearDown() {
        try {
            ApplicationManager.getApplication().registerServiceInstance(
                SettingsRepository::class.java,
                SettingsRepositoryImpl()
            )
            ApplicationManager.getApplication().registerServiceInstance(
                VimTipRepository::class.java,
                VimTipRepositoryImpl()
            )
            project.registerServiceInstance(TipNotifier::class.java, IntelliJTipNotifier(project))
        } finally {
            super.tearDown()
        }
    }

    fun testEveryTipIsShownOnceBeforeAnyRepeats() {
        val tips = (1..5).map { VimTip("tip-$it", listOf("details-$it")) }
        tipRepository.saveTips(tips)

        val firstCycle = showTipSummaries(tips.size)
        val secondCycle = showTipSummaries(tips.size)

        assertEquals(tips.map { it.summary }.toSet(), firstCycle.toSet())
        assertEquals(tips.map { it.summary }.toSet(), secondCycle.toSet())
    }

    fun testExcludedTipIsNeverShownEvenAcrossCycleResets() {
        val hiddenTip = VimTip("hidden", listOf("hidden-details"))
        val visibleTips = (1..3).map { VimTip("visible-$it", listOf("details-$it")) }
        tipRepository.saveTips(visibleTips + hiddenTip)
        settings.hideTip(TipHash.fromTip(hiddenTip).value)

        val twoFullCycles = showTipSummaries(visibleTips.size * 2)

        assertFalse(twoFullCycles.contains("hidden"))
        assertEquals(visibleTips.map { it.summary }.toSet(), twoFullCycles.toSet())
    }

    fun testTipsFromDisabledCategoriesAreNotShown() {
        tipRepository.saveTips(
            listOf(
                VimTip("editing-tip", listOf("details"), listOf("editing")),
                VimTip("search-tip", listOf("details"), listOf("search"))
            )
        )
        settings.setEnabledTipCategories(listOf("editing", "search"), listOf("editing"))

        assertEquals(setOf("editing-tip"), showTipSummaries(4).toSet())
    }

    fun testAdvancedTipsAreShownOnlyAfterOptIn() {
        tipRepository.saveTips(
            listOf(
                VimTip("advanced-tip", listOf("details"), advanced = true),
                VimTip("normal-tip", listOf("details"))
            )
        )

        val beforeOptIn = showTipSummaries(4)
        settings.setShowAdvancedTipsEnabled(true)
        val afterOptIn = showTipSummaries(2)

        assertEquals(setOf("normal-tip"), beforeOptIn.toSet())
        assertTrue(afterOptIn.contains("advanced-tip"))
    }

    // The test IDE bundles IdeaVim (platformPlugins in gradle.properties), so production wiring
    // resolves IdeaVim availability to true — only the include direction of the config-tip filter
    // is reachable at this seam. The exclude direction stays covered by unit tests.
    fun testConfigTipsAreShownWhenIdeaVimIsAvailable() {
        tipRepository.saveTips(
            listOf(
                VimTip("config-tip", listOf("details"), config = TipConfig(lines = listOf("set number"))),
                VimTip("plain-tip", listOf("details"))
            )
        )

        assertEquals(setOf("config-tip", "plain-tip"), showTipSummaries(2).toSet())
    }

    fun testFallbackWhenNoTipsAreLoaded() {
        showTips.showRandomTip()

        assertEquals("No tips found.", notifier.shownTips.single().summary)
    }

    fun testFilteredFallbackWhenEveryCategoryIsDisabled() {
        tipRepository.saveTips(listOf(VimTip("editing-tip", listOf("details"), listOf("editing"))))
        settings.setEnabledTipCategories(listOf("editing"), emptyList())

        showTips.showRandomTip()

        assertEquals("No tips match the selected categories.", notifier.shownTips.single().summary)
    }

    private fun showTipSummaries(count: Int): List<String> {
        return (1..count).map {
            showTips.showRandomTip()
            notifier.shownTips.last().summary
        }
    }

    private class RecordingTipNotifier : TipNotifier {
        val shownTips = mutableListOf<VimTip>()

        override fun hasVisibleTip(): Boolean = false
        override fun showTip(tip: VimTip, actions: TipActions) {
            shownTips.add(tip)
        }
        override fun showTipExcluded(onManage: () -> Unit) = Unit
        override fun showAdvancedTipsAvailable(onOpenSettings: () -> Unit) = Unit
        override fun showAddedToIdeaVimRc(onReload: (() -> Unit)?): TipMessageHandle =
            object : TipMessageHandle { override fun dismiss() = Unit }
        override fun showAlreadyInIdeaVimRc() = Unit
        override fun showCreateIdeaVimRcGuidance() = Unit
        override fun showAddToIdeaVimRcFailed(reason: AddTipToIdeaVimRc.FailureReason) = Unit
        override fun showReloadedIdeaVimRc() = Unit
        override fun showReloadIdeaVimRcFailed() = Unit
    }
}
