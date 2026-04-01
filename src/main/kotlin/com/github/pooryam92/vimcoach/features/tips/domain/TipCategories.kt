package com.github.pooryam92.vimcoach.features.tips.domain

data class TipCategories(
    var values: List<String> = emptyList()
) {
    fun isEmpty(): Boolean = values.isEmpty()

    fun isNotEmpty(): Boolean = values.isNotEmpty()

    companion object {
        fun fromTips(tips: List<VimTip>): TipCategories {
            val categories = tips
                .asSequence()
                .flatMap { it.category.asSequence() }
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .toList()
            return TipCategories(categories)
        }
    }
}
