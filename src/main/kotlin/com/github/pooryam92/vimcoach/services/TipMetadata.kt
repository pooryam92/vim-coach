package com.github.pooryam92.vimcoach.services

/**
 * Metadata for tracking changes in tip content from GitHub.
 * Uses ETag and GitHub SHA for efficient conditional requests.
 */
data class TipMetadata(
    var etag: String? = null,
    var githubSha: String? = null,
    var lastFetchTimestamp: Long = 0
)
