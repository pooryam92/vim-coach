package com.github.pooryam92.vimcoach.features.tips.source.infra.remote

import com.github.pooryam92.vimcoach.features.tips.source.infra.config.VimTipConfig
import com.github.pooryam92.vimcoach.features.tips.source.infra.parsing.TipJsonParser
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import java.net.HttpURLConnection
import java.util.Base64

class RemoteTipSourceServiceImpl : RemoteTipSourceService {
    private val gson = Gson()

    override fun loadTips(): TipSourceLoadResult {
        logger.info("Loading Vim tips from remote source (unconditional)")
        return loadTipsConditional(TipMetadata())
    }

    override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
        logger.info(
            "Loading Vim tips from remote source (conditional=true, hasEtag=${metadata.etag != null}, hasSha=${metadata.githubSha != null})"
        )
        return try {
            val context = RequestContext()
            val tips = fetchTipsFromRemote(metadata, context)
            toLoadResult(tips, context)
        } catch (e: Exception) {
            logger.warn("Remote tip source failed", e)
            TipSourceLoadResult.Failure(e.message ?: "Unknown error", e)
        }
    }

    private fun fetchTipsFromRemote(metadata: TipMetadata, context: RequestContext): List<VimTip> {
        return HttpRequests.request(VimTipConfig.GITHUB_API_URL)
            .tuner { connection ->
                connection.setRequestProperty(HEADER_ACCEPT, ACCEPT_GITHUB_V3_JSON)
                metadata.etag?.let { connection.setRequestProperty(HEADER_IF_NONE_MATCH, it) }
            }
            .connect { request ->
                val connection = request.connection as? HttpURLConnection
                if (connection?.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    logger.info("Remote tip source responded with 304 Not Modified")
                    context.notModified = true
                    return@connect emptyList()
                }

                context.responseETag = connection?.getHeaderField(HEADER_ETAG)

                val apiResponse = gson.fromJson(request.reader, GitHubApiResponse::class.java)
                context.githubSha = apiResponse.sha

                if (isSameSha(metadata, context.githubSha)) {
                    logger.info("Remote tip source SHA unchanged; treating as not modified")
                    context.notModified = true
                    return@connect emptyList()
                }

                parseTipsFromApiResponse(apiResponse)
            }
    }

    private fun parseTipsFromApiResponse(apiResponse: GitHubApiResponse): List<VimTip> {
        val decodedContent = Base64.getDecoder().decode(
            apiResponse.content
                .replace("\n", "")
                .replace("\r", "")
        )
        return TipJsonParser.parseTipsJson(decodedContent.inputStream())
    }

    private fun toLoadResult(tips: List<VimTip>, context: RequestContext): TipSourceLoadResult {
        if (context.notModified) {
            return TipSourceLoadResult.NotModified
        }
        if (tips.isEmpty()) {
            logger.info("Remote tip source returned no valid tips")
            return TipSourceLoadResult.Empty
        }
        logger.info("Remote tip source returned ${tips.size} tips")
        val newMetadata = TipMetadata(
            etag = context.responseETag,
            githubSha = context.githubSha,
            lastFetchTimestamp = System.currentTimeMillis()
        )
        return TipSourceLoadResult.Success(tips, newMetadata)
    }

    private fun isSameSha(metadata: TipMetadata, githubSha: String?): Boolean {
        return metadata.githubSha != null && metadata.githubSha == githubSha
    }

    private data class RequestContext(
        var responseETag: String? = null,
        var githubSha: String? = null,
        var notModified: Boolean = false
    )

    private companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_IF_NONE_MATCH = "If-None-Match"
        const val HEADER_ETAG = "ETag"
        const val ACCEPT_GITHUB_V3_JSON = "application/vnd.github.v3+json"
        val logger = Logger.getInstance(RemoteTipSourceServiceImpl::class.java)
    }
}
