package com.github.pooryam92.vimcoach.features.tips.unit.application.selection

import com.github.pooryam92.vimcoach.features.tips.application.selection.TipSelectionContext
import com.github.pooryam92.vimcoach.features.tips.application.selection.advancedTipsFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.categoryFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.configTipsFilter
import com.github.pooryam92.vimcoach.features.tips.application.selection.excludedTipsFilter
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import org.junit.Assert.assertEquals
import org.junit.Test

class TipFiltersUnitTest {

    @Test
    fun categoryFilterKeepsOnlyTipsInAnEnabledCategoryButPassesThroughWhenNoCategoriesExist() {
        val editingTip = vimTip("editing", listOf("details"), listOf("editing"))
        val searchTip = vimTip("search", listOf("details"), listOf("search"))
        val pool = listOf(editingTip, searchTip)
        val context = contextFor(availableCategories = listOf("editing", "search"), enabledCategories = listOf("editing"))

        assertEquals(listOf(editingTip), categoryFilter.apply(pool, context))
        assertEquals(pool, categoryFilter.apply(pool, context.copy(availableCategories = emptyList())))
    }

    @Test
    fun excludedTipsFilterDropsHiddenTips() {
        val hiddenTip = vimTip("hidden", listOf("details"))
        val visibleTip = vimTip("visible", listOf("details"))
        val pool = listOf(hiddenTip, visibleTip)
        val context = contextFor(hiddenTipIds = setOf(hiddenTip.id))

        assertEquals(listOf(visibleTip), excludedTipsFilter.apply(pool, context))
    }

    @Test
    fun configTipsFilterDropsConfigTipsOnlyWhenIncludeConfigTipsIsFalse() {
        val configTip = vimTip("config", listOf("details"), config = TipConfig(lines = listOf("set number")))
        val plainTip = vimTip("plain", listOf("details"))
        val pool = listOf(configTip, plainTip)

        assertEquals(listOf(plainTip), configTipsFilter.apply(pool, contextFor(includeConfigTips = false)))
        assertEquals(pool, configTipsFilter.apply(pool, contextFor(includeConfigTips = true)))
    }

    @Test
    fun advancedTipsFilterDropsAdvancedTipsOnlyWhenSettingIsOff() {
        val advancedTip = vimTip("advanced", listOf("details"), advanced = true)
        val normalTip = vimTip("normal", listOf("details"))
        val pool = listOf(advancedTip, normalTip)

        assertEquals(listOf(normalTip), advancedTipsFilter.apply(pool, contextFor(showAdvancedTips = false)))
        assertEquals(pool, advancedTipsFilter.apply(pool, contextFor(showAdvancedTips = true)))
    }

    private fun contextFor(
        availableCategories: List<String> = emptyList(),
        enabledCategories: List<String> = emptyList(),
        hiddenTipIds: Set<String> = emptySet(),
        showAdvancedTips: Boolean = false,
        includeConfigTips: Boolean = true,
    ) = TipSelectionContext(
        availableCategories = availableCategories,
        enabledCategories = enabledCategories,
        hiddenTipIds = hiddenTipIds,
        showAdvancedTips = showAdvancedTips,
        includeConfigTips = includeConfigTips,
    )
}
