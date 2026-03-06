package com.github.pooryam92.vimcoach.features.tips.source.infra.remote

import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult

interface RemoteTipSourceService {
    fun loadTips(): TipSourceLoadResult
    fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult
}
