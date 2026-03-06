package com.github.pooryam92.vimcoach.features.tips.source.application

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult

interface TipSourceService {
    fun loadTips(): TipSourceLoadResult
    fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult
}
