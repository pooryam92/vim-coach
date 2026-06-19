package com.github.pooryam92.vimcoach.features.tips.integration.loading.infra.remote

import com.github.pooryam92.vimcoach.features.tips.application.loading.infra.remote.RemoteTipSourceServiceImpl
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.net.InetSocketAddress
import java.util.Base64

/**
 * Drives [RemoteTipSourceServiceImpl] against a real local HTTP server so the actual
 * HttpRequests wiring is exercised: conditional headers, 304/SHA dedup, Base64 decode,
 * and error mapping. The GitHub URL constant is overridden via the constructor seam.
 */
class RemoteTipSourceServiceIntTest : BasePlatformTestCase() {

    private lateinit var server: HttpServer
    private lateinit var sourceUrl: String

    private var responder: (HttpExchange) -> Unit = { respondOk(it) }
    private var lastIfNoneMatch: String? = null
    private var lastAccept: String? = null

    override fun setUp() {
        super.setUp()
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/tips") { exchange ->
            lastIfNoneMatch = exchange.requestHeaders.getFirst("If-None-Match")
            lastAccept = exchange.requestHeaders.getFirst("Accept")
            responder(exchange)
            exchange.close()
        }
        server.start()
        sourceUrl = "http://127.0.0.1:${server.address.port}/tips"
    }

    override fun tearDown() {
        try {
            server.stop(0)
        } finally {
            super.tearDown()
        }
    }

    fun testLoadTipsReturnsSuccessWithTipsAndMetadata() {
        responder = { respondOk(it, sha = "sha-1", etag = "\"etag-1\"", tipsJson = TWO_TIPS_JSON) }

        val result = service().loadTips() as TipSourceLoadResult.Success

        assertEquals(2, result.tips.size)
        assertEquals("summary-1", result.tips[0].summary)
        assertEquals("sha-1", result.metadata.githubSha)
        assertEquals("\"etag-1\"", result.metadata.etag)
        assertTrue("timestamp should be stamped", result.metadata.lastFetchTimestamp > 0)
    }

    fun testLoadTipsUnconditionalSendsAcceptButNoIfNoneMatch() {
        responder = { respondOk(it) }

        service().loadTips()

        assertEquals("application/vnd.github.v3+json", lastAccept)
        assertNull("unconditional fetch must not send If-None-Match", lastIfNoneMatch)
    }

    fun testConditionalFetchSendsIfNoneMatchAndHandles304() {
        responder = { exchange ->
            exchange.sendResponseHeaders(304, -1)
        }

        val result = service().loadTipsConditional(TipMetadata(etag = "\"cached-etag\""))

        assertEquals(TipSourceLoadResult.NotModified, result)
        assertEquals("\"cached-etag\"", lastIfNoneMatch)
    }

    fun testUnchangedShaIsTreatedAsNotModified() {
        responder = { respondOk(it, sha = "same-sha", tipsJson = TWO_TIPS_JSON) }

        val result = service().loadTipsConditional(TipMetadata(githubSha = "same-sha"))

        assertEquals(TipSourceLoadResult.NotModified, result)
    }

    fun testValidResponseWithNoTipsReturnsEmpty() {
        responder = { respondOk(it, tipsJson = """{"tips":[]}""") }

        val result = service().loadTips()

        assertEquals(TipSourceLoadResult.Empty, result)
    }

    fun testMalformedJsonReturnsFailure() {
        responder = { exchange ->
            val body = "not-json".toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.write(body)
        }

        val result = service().loadTips()

        assertTrue(result is TipSourceLoadResult.Failure)
    }

    fun testServerErrorReturnsFailure() {
        responder = { exchange ->
            exchange.sendResponseHeaders(500, -1)
        }

        val result = service().loadTips()

        assertTrue(result is TipSourceLoadResult.Failure)
    }

    private fun service() = RemoteTipSourceServiceImpl(sourceUrl)

    private fun respondOk(
        exchange: HttpExchange,
        sha: String = "sha",
        etag: String? = "\"etag\"",
        tipsJson: String = TWO_TIPS_JSON
    ) {
        val encoded = Base64.getEncoder().encodeToString(tipsJson.toByteArray())
        val body = """{"name":"vim_tips_min.json","sha":"$sha","size":${tipsJson.length},""" +
            """"content":"$encoded","encoding":"base64"}"""
        etag?.let { exchange.responseHeaders.add("ETag", it) }
        val bytes = body.toByteArray()
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }

    private companion object {
        const val TWO_TIPS_JSON =
            """{"tips":[{"summary":"summary-1","details":["details-1"]},""" +
                """{"summary":"summary-2","details":["details-2"]}]}"""
    }
}
