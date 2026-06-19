package com.github.pooryam92.vimcoach.features.tips.domain

sealed interface TipSourceLoadResult {
    data class Success(val tips: List<VimTip>, val metadata: TipMetadata) : TipSourceLoadResult
    data object NotModified : TipSourceLoadResult
    data object Empty : TipSourceLoadResult
    data class Failure(val message: String, val cause: Throwable? = null) : TipSourceLoadResult
}
