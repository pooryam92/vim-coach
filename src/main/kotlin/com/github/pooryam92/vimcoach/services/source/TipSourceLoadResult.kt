package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.services.TipMetadata
import com.github.pooryam92.vimcoach.services.VimTip

sealed interface TipSourceLoadResult {
    data class Success(val tips: List<VimTip>, val metadata: TipMetadata) : TipSourceLoadResult
    data object NotModified : TipSourceLoadResult
    data object Empty : TipSourceLoadResult
    data class Failure(val message: String, val cause: Throwable? = null) : TipSourceLoadResult
}
