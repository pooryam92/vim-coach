package com.github.pooryam92.vimcoach.features.tips.domain

/**
 * Metadata for tracking changes in tip content from GitHub.
 * Uses ETag and GitHub SHA for efficient conditional requests.
 *
 * [pluginVersion] records which plugin version parsed the cached tips. The conditional (ETag/SHA)
 * fetch is keyed only on the *remote bytes*, so a plugin upgrade that extracts more from the same
 * remote JSON (e.g. .ideavimrc configs) would otherwise keep serving the stale, under-parsed cache
 * after a 304. When the cached [pluginVersion] differs from the running one, the startup check forces
 * one unconditional refetch so the new parser re-runs. Legacy caches deserialize to null and
 * self-heal on the first run of an upgraded build.
 */
data class TipMetadata(
    var etag: String? = null,
    var githubSha: String? = null,
    var lastFetchTimestamp: Long = 0,
    var pluginVersion: String? = null
)
