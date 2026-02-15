package com.github.pooryam92.vimcoach.services

sealed interface RemoteTipLoadResult {
    data class Success(val tips: List<VimTip>) : RemoteTipLoadResult
    data object Empty : RemoteTipLoadResult
    data class Failure(val message: String, val cause: Throwable? = null) : RemoteTipLoadResult
}

sealed interface TipLoadResult {
    data class Updated(val tipCount: Int) : TipLoadResult
    data object SkippedAlreadyLoaded : TipLoadResult
    data object NoData : TipLoadResult
    data class Failed(val message: String, val cause: Throwable? = null) : TipLoadResult
}
