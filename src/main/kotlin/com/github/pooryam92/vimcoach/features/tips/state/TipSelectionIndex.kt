package com.github.pooryam92.vimcoach.features.tips.state

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

internal class TipSelectionIndex private constructor(
    private val sourceTips: List<VimTip>,
    private val tipsByCategory: Map<String, List<VimTip>>
) {
    fun isFor(tips: List<VimTip>): Boolean {
        return sourceTips === tips
    }

    fun matchingTips(categories: List<String>): List<VimTip> {
        val normalizedCategories = normalizeCategories(categories)
        if (normalizedCategories.isEmpty()) {
            return emptyList()
        }

        if (normalizedCategories.size == 1) {
            val category = normalizedCategories.first()
            return tipsByCategory[category].orEmpty()
        }

        val matchingTips = linkedSetOf<VimTip>()
        normalizedCategories.forEach { category ->
            tipsByCategory[category]?.forEach(matchingTips::add)
        }
        return matchingTips.toList()
    }

    companion object {
        fun fromTips(tips: List<VimTip>): TipSelectionIndex {
            val tipsByCategory = linkedMapOf<String, MutableList<VimTip>>()

            tips.forEach { tip ->
                normalizeCategories(tip.category).forEach { category ->
                    tipsByCategory.getOrPut(category) { mutableListOf() }.add(tip)
                }
            }

            return TipSelectionIndex(
                sourceTips = tips,
                tipsByCategory = tipsByCategory.mapValues { (_, matchingTips) -> matchingTips.toList() }
            )
        }

        private fun normalizeCategories(categories: List<String>): Set<String> {
            return categories
                .asSequence()
                .map(String::trim)
                .filter(String::isNotBlank)
                .toSet()
        }
    }
}
