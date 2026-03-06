package com.github.pooryam92.vimcoach.features.tips.domain

sealed interface TipLoadResult {
    data class Updated(val tipCount: Int) : TipLoadResult
    data object NotModified : TipLoadResult
    data object NoData : TipLoadResult
    data class Failed(val message: String, val cause: Throwable? = null) : TipLoadResult
}
