package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.TipSelectionContext
import com.github.pooryam92.vimcoach.features.tips.application.selection.advancedTipsFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.categoryFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.configTipsFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.excludedTipsFilter
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import org.junit.Assert.assertEquals
import org.junit.Test

class TipFiltersUnitTest {

    @Test
    fun categoryFilterKeepsOnlyTipsInAnEnabledCategoryButPassesThroughWhenNoCategoriesExist() {
        val editingTip = VimTip("editing", listOf("details"), listOf("editing"))
        val searchTip = VimTip("search", listOf("details"), listOf("search"))
        val pool = listOf(editingTip, searchTip)
        val context = contextFor(availableCategories = listOf("editing", "search"), enabledCategories = listOf("editing"))

        assertEquals(listOf(editingTip), categoryFilter.apply(pool, context))
        assertEquals(pool, categoryFilter.apply(pool, context.copy(availableCategories = emptyList())))
    }

    @Test
    fun excludedTipsFilterDropsHiddenTips() {
        val hiddenTip = VimTip("hidden", listOf("details"))
        val visibleTip = VimTip("visible", listOf("details"))
        val pool = listOf(hiddenTip, visibleTip)
        val context = contextFor(hiddenTipHashes = setOf(TipHash.fromTip(hiddenTip).value))

        assertEquals(listOf(visibleTip), excludedTipsFilter.apply(pool, context))
    }

    @Test
    fun configTipsFilterDropsConfigTipsOnlyWhenIncludeConfigTipsIsFalse() {
        val configTip = VimTip("config", listOf("details"), config = TipConfig(lines = listOf("set number")))
        val plainTip = VimTip("plain", listOf("details"))
        val pool = listOf(configTip, plainTip)

        assertEquals(listOf(plainTip), configTipsFilter.apply(pool, contextFor(includeConfigTips = false)))
        assertEquals(pool, configTipsFilter.apply(pool, contextFor(includeConfigTips = true)))
    }

    @Test
    fun advancedTipsFilterDropsAdvancedTipsOnlyWhenSettingIsOff() {
        val advancedTip = VimTip("advanced", listOf("details"), advanced = true)
        val normalTip = VimTip("normal", listOf("details"))
        val pool = listOf(advancedTip, normalTip)

        assertEquals(listOf(normalTip), advancedTipsFilter.apply(pool, contextFor(showAdvancedTips = false)))
        assertEquals(pool, advancedTipsFilter.apply(pool, contextFor(showAdvancedTips = true)))
    }

    private fun contextFor(
        availableCategories: List<String> = emptyList(),
        enabledCategories: List<String> = emptyList(),
        hiddenTipHashes: Set<String> = emptySet(),
        showAdvancedTips: Boolean = false,
        includeConfigTips: Boolean = true,
    ) = TipSelectionContext(
        availableCategories = availableCategories,
        enabledCategories = enabledCategories,
        hiddenTipHashes = hiddenTipHashes,
        showAdvancedTips = showAdvancedTips,
        includeConfigTips = includeConfigTips,
    )
}
