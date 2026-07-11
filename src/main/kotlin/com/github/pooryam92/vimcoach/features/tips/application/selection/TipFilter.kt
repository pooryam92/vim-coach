package com.github.pooryam92.vimcoach.features.tips.application.selection

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip

internal fun interface TipFilter {
    fun apply(pool: List<VimTip>, context: TipSelectionContext): List<VimTip>
}

/** No categories exist yet (tips not loaded, or none carry a category) → pass every tip through. */
internal val categoryFilter = TipFilter { pool, context ->
    if (context.availableCategories.isEmpty()) {
        pool
    } else {
        pool.filter { tip -> tip.category.any { it in context.enabledCategories } }
    }
}

internal val excludedTipsFilter = TipFilter { pool, context ->
    pool.filterNot { TipHash.fromTip(it).value in context.hiddenTipHashes }
}

/**
 * [TipSelectionContext.includeConfigTips] = false drops tips carrying an .ideavimrc snippet
 * ([VimTip.config]). Such tips are only actionable when IdeaVim is installed (their "Add to
 * .ideavimrc" button needs it), so callers without IdeaVim exclude them rather than show an
 * inapplicable tip.
 */
internal val configTipsFilter = TipFilter { pool, context ->
    pool.filter { context.includeConfigTips || it.config?.lines.isNullOrEmpty() }
}

internal val advancedTipsFilter = TipFilter { pool, context ->
    pool.filter { context.showAdvancedTips || !it.advanced }
}
