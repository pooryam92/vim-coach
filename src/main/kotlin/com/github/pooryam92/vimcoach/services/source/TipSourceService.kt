package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.services.TipMetadata

interface TipSourceService {
    fun loadTips(): TipSourceLoadResult
    fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult
}
