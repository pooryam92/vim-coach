package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.domain.TipCategories
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.FakeSettingsService
import org.junit.Assert.assertEquals
import org.junit.Test

class VimTipRepositoryAdvancedFilterUnitTest {

    private val advancedTip = VimTip("advanced tip", listOf("details"), advanced = true)
    private val normalTip = VimTip("normal tip", listOf("details"))

    @Test
    fun hidesAdvancedTipsWhenOptInDisabled() {
        val repository = VimTipRepositoryImpl(storeWith(advancedTip), FakeSettingsService(showAdvancedTips = false))

        assertEquals("No tips found.", repository.getRandomTip().summary)
    }

    @Test
    fun showsAdvancedTipsWhenOptInEnabled() {
        val repository = VimTipRepositoryImpl(storeWith(advancedTip), FakeSettingsService(showAdvancedTips = true))

        assertEquals("advanced tip", repository.getRandomTip().summary)
    }

    @Test
    fun keepsNormalTipsWhileHidingAdvancedOnes() {
        val repository =
            VimTipRepositoryImpl(storeWith(normalTip, advancedTip), FakeSettingsService(showAdvancedTips = false))

        assertEquals("normal tip", repository.getRandomTip().summary)
    }

    // No settings service (e.g. an unconfigured cache outside a project) must hide advanced tips — the
    // safe default from show-tip.md. The single-arg constructor injects no settings and the platform
    // service lookup fails in a plain unit test, so this exercises exactly that null fallback.
    @Test
    fun hidesAdvancedTipsWhenSettingsServiceUnavailable() {
        val repository = VimTipRepositoryImpl(storeWith(advancedTip))

        assertEquals("No tips found.", repository.getRandomTip().summary)
    }

    private fun storeWith(vararg tips: VimTip): PersistentVimTipStore {
        val tipList = tips.toList()
        return PersistentVimTipStore().apply { setTipCache(tipList, TipCategories.fromTips(tipList)) }
    }
}
