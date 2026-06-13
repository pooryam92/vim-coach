package com.github.pooryam92.vimcoach.features.tips.application.loading

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult

interface TipSourceService {
    fun loadTips(): TipSourceLoadResult
    fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult
}
