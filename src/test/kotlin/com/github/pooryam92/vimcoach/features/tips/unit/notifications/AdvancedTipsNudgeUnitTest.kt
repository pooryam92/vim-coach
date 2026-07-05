package com.github.pooryam92.vimcoach.features.tips.unit.notifications

import com.github.pooryam92.vimcoach.features.tips.application.notifications.AdvancedTipsNudge
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeVimTipRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdvancedTipsNudgeUnitTest {

    private val advancedTip = VimTip("advanced tip", listOf("details"), advanced = true)

    @Test
    fun nudgesOnceOnTheThirdEligibleTip() {
        val nudge = nudge(settings())

        assertFalse("first tip", nudge.shouldNudgeAfterTipShown())
        assertFalse("second tip", nudge.shouldNudgeAfterTipShown())
        assertTrue("third tip", nudge.shouldNudgeAfterTipShown())
        assertFalse("fourth tip — hint already consumed", nudge.shouldNudgeAfterTipShown())
    }

    @Test
    fun neverNudgesWhenSettingIsOn() {
        // Loaded state can hold setting-on with the hint unconsumed (persisted by a version that
        // didn't retire the hint on enable), so the setting guard is pinned independently.
        val store = PersistentSettingsStore()
        store.loadState(PersistentSettingsStore.State(showAdvancedTips = true))
        val nudge = nudge(SettingsRepositoryImpl(store))

        repeat(5) { assertFalse(nudge.shouldNudgeAfterTipShown()) }
    }

    @Test
    fun neverNudgesAfterUserDiscoveredTheToggleThemselves() {
        val settings = settings()
        settings.setShowAdvancedTipsEnabled(true)
        settings.setShowAdvancedTipsEnabled(false)
        val nudge = nudge(settings)

        repeat(5) { assertFalse(nudge.shouldNudgeAfterTipShown()) }
    }

    @Test
    fun neverNudgesWhenCacheHasNoAdvancedTips() {
        val nudge = AdvancedTipsNudge(
            settings(),
            FakeVimTipRepository(initialTips = listOf(VimTip("normal tip", listOf("details"))))
        )

        repeat(5) { assertFalse(nudge.shouldNudgeAfterTipShown()) }
    }

    @Test
    fun doesNotSpendTheCountWhileNoAdvancedTipsAreAvailable() {
        val settings = settings()
        val repository = FakeVimTipRepository(initialTips = listOf(VimTip("normal tip", listOf("details"))))
        val nudge = AdvancedTipsNudge(settings, repository)
        repeat(5) { nudge.shouldNudgeAfterTipShown() }

        repository.saveTips(listOf(advancedTip))

        assertFalse("first eligible tip", nudge.shouldNudgeAfterTipShown())
        assertFalse("second eligible tip", nudge.shouldNudgeAfterTipShown())
        assertTrue("third eligible tip", nudge.shouldNudgeAfterTipShown())
    }

    private fun settings(): SettingsRepository = SettingsRepositoryImpl(PersistentSettingsStore())

    private fun nudge(settings: SettingsRepository) =
        AdvancedTipsNudge(settings, FakeVimTipRepository(initialTips = listOf(advancedTip)))
}
