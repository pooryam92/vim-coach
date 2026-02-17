package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.config.VimTipConfig
import com.github.pooryam92.vimcoach.services.TipJsonParser
import com.github.pooryam92.vimcoach.services.TipMetadata
import com.google.gson.Gson
import com.intellij.util.io.HttpRequests
import java.net.HttpURLConnection
import java.util.Base64

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    
    override fun loadTips(): TipSourceLoadResult {
        return loadTipsConditional(TipMetadata())
    }

    override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
        return try {
            var responseETag: String? = null
            var notModified = false
            var githubSha: String? = null

            val tips = HttpRequests.request(VimTipConfig.GITHUB_API_URL)
                .tuner { connection ->
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    
                    // Add conditional request header if we have ETag from previous fetch
                    metadata.etag?.let { etag ->
                        connection.setRequestProperty("If-None-Match", etag)
                    }
                }
                .connect { request ->
                    val connection = request.connection as? HttpURLConnection

                    // Check if GitHub returned 304 Not Modified
                    if (connection?.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        notModified = true
                        return@connect emptyList()
                    }

                    // Capture ETag for future conditional requests
                    responseETag = connection?.getHeaderField("ETag")

                    // Parse GitHub API response
                    val gson = Gson()
                    val apiResponse = gson.fromJson(
                        request.reader,
                        GitHubApiResponse::class.java
                    )

                    githubSha = apiResponse.sha

                    // Check if SHA matches (additional check even if ETag is different)
                    if (metadata.githubSha != null && metadata.githubSha == githubSha) {
                        notModified = true
                        return@connect emptyList()
                    }

                    // Decode base64 content
                    val decodedContent = Base64.getDecoder().decode(
                        apiResponse.content.replace("\n", "").replace("\r", "")
                    )

                    // Parse tips from decoded JSON
                    TipJsonParser.parseTipsJson(decodedContent.inputStream())
                }

            when {
                notModified -> TipSourceLoadResult.NotModified
                tips.isEmpty() -> TipSourceLoadResult.Empty
                else -> {
                    val newMetadata = TipMetadata(
                        etag = responseETag,
                        githubSha = githubSha,
                        lastFetchTimestamp = System.currentTimeMillis()
                    )
                    TipSourceLoadResult.Success(tips, newMetadata)
                }
            }
        } catch (e: Exception) {
            TipSourceLoadResult.Failure(e.message ?: "Unknown error", e)
        }
    }
}
