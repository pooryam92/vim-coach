package com.github.pooryam92.vimcoach.features.tips.integration.application

import com.github.pooryam92.vimcoach.features.tips.application.loading.RefreshTips
import com.github.pooryam92.vimcoach.features.tips.application.loading.TipRefresh
import com.github.pooryam92.vimcoach.features.tips.domain.TipLoadResult
import com.github.pooryam92.vimcoach.features.tips.domain.TipConfig
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.application.loading.TipSourceService
import com.github.pooryam92.vimcoach.features.tips.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.persistence.SettingsRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.VimTipRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentVimTipStore
import com.github.pooryam92.vimcoach.features.tips.testsupport.vimTip
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TipRefreshRefetchIntTest : BasePlatformTestCase() {

    private val CURRENT_PLUGIN_VERSION = "9.9.9-test"

    fun testRefetchTipsReloadsEvenWhenTipsExist() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(listOf(vimTip("old-summary", listOf("old-details"))))

        val remoteTips = listOf(
            vimTip("new-summary-1", listOf("new-details-1"), listOf("motions")),
            vimTip("new-summary-2", listOf("new-details-2"), listOf("editing", "motions"))
        )
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(remoteTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
        assertEquals(listOf("motions", "editing"), tipService.getCategories().values)
        assertEquals(remoteTips, tipService.getTips())
    }

    fun testRefetchTipsUpdatesExistingTips() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(
                vimTip("initial-1", listOf("initial-details-1")),
                vimTip("initial-2", listOf("initial-details-2")),
                vimTip("initial-3", listOf("initial-details-3"))
            )
        )
        assertEquals(3, tipService.countTips())

        val updatedTips = listOf(vimTip("updated-1", listOf("updated-details-1")))
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        assertEquals("updated-1", tipService.getTips().single().summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsFailure() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(listOf(vimTip("existing", listOf("existing-details"))))

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.Failure("connection timeout"))
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Failed("connection timeout"), result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getTips().single().summary)
    }

    fun testRefetchTipsDoesNotSaveWhenRemoteReturnsEmpty() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(listOf(vimTip("existing", listOf("existing-details"))))

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.Empty)
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NoData, result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getTips().single().summary)
    }

    fun testRefetchTipsReturnsNotModifiedWhenNoChanges() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(vimTip("existing", listOf("existing-details"), listOf("motions")))
        )

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        assertEquals(listOf("motions"), tipService.getCategories().values)
        assertEquals("existing", tipService.getTips().single().summary)
    }

    fun testRefetchTipsAlwaysForcesReload() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(listOf(vimTip("existing", listOf("existing-details"))))
        tipService.saveMetadata(
            TipMetadata(
                etag = "abc123",
                githubSha = "def456",
                lastFetchTimestamp = System.currentTimeMillis()
            )
        )

        val updatedTips = listOf(vimTip("new", listOf("new-details")))
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.refetchTips()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
        assertEquals("new", tipService.getTips().single().summary)
    }

    fun testCheckForUpdatesLoadsWhenNoTipsExist() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(emptyList())

        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(listOf(vimTip("new-tip", listOf("new-details"))), TipMetadata())
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals(1, tipService.countTips())
    }

    fun testCheckForUpdatesUsesConditionalWhenCachedCategoriesExist() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(vimTip("existing", listOf("existing-details"), listOf("motions")))
        )
        tipService.saveMetadata(
            TipMetadata(
                etag = "abc123",
                githubSha = "def456",
                lastFetchTimestamp = System.currentTimeMillis() - 3_600_000,
                pluginVersion = CURRENT_PLUGIN_VERSION
            )
        )

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(1, tipService.countTips())
        assertEquals("existing", tipService.getTips().single().summary)
    }

    // TODO(1.6.0 upgrade bridge)
    fun testCheckForUpdatesReloadsLegacyCachedTipsWithoutCategories() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(
                vimTip("legacy-summary-1", listOf("legacy-details-1")),
                vimTip("legacy-summary-2", listOf("legacy-details-2"))
            )
        )
        tipService.saveMetadata(
            TipMetadata(
                etag = "etag",
                githubSha = "sha",
                lastFetchTimestamp = System.currentTimeMillis() - 3_600_000
            )
        )

        val refreshedTips = listOf(
            vimTip("new-summary-1", listOf("new-details-1"), listOf("basics")),
            vimTip("new-summary-2", listOf("new-details-2"), listOf("editing", "basics"))
        )
        val fakeTipSource = FakeTipSource(
            loadTipsResult = TipSourceLoadResult.Success(
                refreshedTips,
                TipMetadata(etag = "new-etag", githubSha = "new-sha")
            ),
            loadTipsConditionalResult = TipSourceLoadResult.NotModified
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(listOf("basics", "editing"), tipService.getCategories().values)
    }

    // TODO(1.6.0 upgrade bridge)
    fun testCheckForUpdatesRefetchesUnconditionallyWhenCachedTipsLackIds() {
        val tipService = service<VimTipRepository>()
        service<PersistentVimTipStore>().loadState(
            PersistentVimTipStore.State(
                tips = listOf(VimTip("pre-id-summary", listOf("pre-id-details"), listOf("motions"))),
                metadata = TipMetadata(etag = "etag", githubSha = "sha", pluginVersion = CURRENT_PLUGIN_VERSION)
            )
        )
        val fakeTipSource = FakeTipSource(
            loadTipsResult = TipSourceLoadResult.Success(
                listOf(vimTip("new-summary", listOf("new-details"), listOf("motions"))),
                TipMetadata(etag = "new-etag", githubSha = "new-sha")
            ),
            loadTipsConditionalResult = TipSourceLoadResult.NotModified
        )

        val result = registerLoader(fakeTipSource).checkForUpdates()

        assertEquals(1, fakeTipSource.loadTipsCalls)
        assertEquals(0, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(1), result)
        assertEquals("new-summary", tipService.getTips().single().summary)
    }

    // TODO(1.6.0 upgrade bridge)
    fun testFetchedTipsCarryOverExclusionsSavedBeforeIds() {
        val settingsStore = service<PersistentSettingsStore>()
        settingsStore.loadState(PersistentSettingsStore.State(hiddenTipHashes = listOf(SURROUND_A_WORD_HASH)))
        val surroundTip = vimTip("Surround a word", listOf("ysiw\""), listOf("motions"))
        val fakeTipSource = FakeTipSource(TipSourceLoadResult.Success(listOf(surroundTip), TipMetadata()))

        try {
            registerLoader(fakeTipSource).refetchTips()

            assertEquals(listOf(surroundTip.id), service<SettingsRepository>().getHiddenTipIds())
        } finally {
            settingsStore.loadState(PersistentSettingsStore.State())
        }
    }

    fun testCheckForUpdatesNotModifiedRefreshesLastFetchTimestamp() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(vimTip("existing", listOf("existing-details"), listOf("motions")))
        )
        val initialTimestamp = 1_000L
        tipService.saveMetadata(
            TipMetadata(
                etag = "etag",
                githubSha = "sha",
                lastFetchTimestamp = initialTimestamp,
                pluginVersion = CURRENT_PLUGIN_VERSION
            )
        )

        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(TipLoadResult.NotModified, result)
        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertTrue(tipService.getMetadata().lastFetchTimestamp > initialTimestamp)
    }

    fun testCheckForUpdatesUpdatesWhenChangesDetected() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(vimTip("old", listOf("old-details"), listOf("motions")))
        )
        tipService.saveMetadata(TipMetadata(etag = "old-etag", pluginVersion = CURRENT_PLUGIN_VERSION))

        val updatedTips = listOf(
            vimTip("new-1", listOf("new-details-1")),
            vimTip("new-2", listOf("new-details-2"))
        )
        val fakeTipSource = FakeTipSource(
            TipSourceLoadResult.Success(updatedTips, TipMetadata(etag = "new-etag"))
        )
        val loader = registerLoader(fakeTipSource)

        val result = loader.checkForUpdates()

        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
        assertEquals(TipLoadResult.Updated(2), result)
        assertEquals(2, tipService.countTips())
    }

    fun testCheckForUpdatesRunsOnlyOncePerLoaderInstance() {
        val tipService = service<VimTipRepository>()
        tipService.saveTips(
            listOf(vimTip("existing", listOf("existing-details"), listOf("motions")))
        )
        tipService.saveMetadata(TipMetadata(pluginVersion = CURRENT_PLUGIN_VERSION))
        val fakeTipSource = FakeTipSource(TipSourceLoadResult.NotModified)
        val loader = registerLoader(fakeTipSource)

        val firstResult = loader.checkForUpdates()
        val secondResult = loader.checkForUpdates()

        assertEquals(TipLoadResult.NotModified, firstResult)
        assertEquals(TipLoadResult.NotModified, secondResult)
        assertEquals(0, fakeTipSource.loadTipsCalls)
        assertEquals(1, fakeTipSource.loadTipsConditionalCalls)
    }

    /**
     * Proves the plugin-upgrade staleness bug. EXPECTED TO FAIL while the bug is present.
     *
     * The conditional-fetch cache is keyed only on the *remote content* (ETag/SHA), not on the
     * local parser version. Scenario from the field:
     *   1. An older plugin version (no .ideavimrc config support) cached the remote tips, parsing
     *      them WITHOUT [VimTip.config], and stored the remote's ETag/SHA.
     *   2. The user upgrades to a config-aware version. On startup checkForUpdates() sees a usable
     *      cache, so it does a *conditional* fetch sending the stored ETag/SHA.
     *   3. The remote bytes are unchanged, so the server replies 304 Not Modified. The new,
     *      config-aware parser never runs, and the cache keeps the config-less tips forever — so
     *      config-bearing tips (and their "Add to .ideavimrc" button) never appear until a manual
     *      unconditional refetch bypasses the ETag.
     *
     * Correct behavior: after a plugin upgrade, the startup check must surface the config-aware
     * parse of the cached remote content rather than serving the stale, under-parsed cache.
     */
    fun testCheckForUpdatesSurfacesConfigsAfterPluginUpgrade() {
        val tipService = service<VimTipRepository>()
        val summary = "Enable OS clipboard"
        // (1) Old plugin cached this tip parsed WITHOUT a config, plus the remote's ETag/SHA.
        tipService.saveTips(listOf(vimTip(summary, listOf("yank to system clipboard"), listOf("clipboard"))))
        tipService.saveMetadata(TipMetadata(etag = "remote-etag", githubSha = "remote-sha"))

        // The config-aware parser produces the SAME remote tip WITH a config.
        val configAwareTip = vimTip(
            summary,
            listOf("yank to system clipboard"),
            listOf("clipboard"),
            TipConfig("Enable OS clipboard", listOf("set clipboard+=unnamedplus"))
        )
        val fakeTipSource = FakeTipSource(
            loadTipsResult = TipSourceLoadResult.Success(
                listOf(configAwareTip),
                TipMetadata(etag = "remote-etag", githubSha = "remote-sha")
            ),
            loadTipsConditionalResult = TipSourceLoadResult.NotModified
        )
        val loader = registerLoader(fakeTipSource)

        // (2)+(3) Startup after upgrade.
        loader.checkForUpdates()

        // Expected after upgrade: the config-aware parse is surfaced. FAILS while the conditional
        // 304 short-circuit serves the stale, config-less cache.
        assertNotNull(tipService.getTips().single { it.summary == summary }.config)
    }

    private fun registerLoader(fakeTipSource: TipSourceService): RefreshTips {
        return TipRefresh(
            tipSource = fakeTipSource,
            currentPluginVersion = { CURRENT_PLUGIN_VERSION }
        )
    }

    private companion object {
        // TODO(1.6.0 upgrade bridge): SHA-256 of "Surround a word", the exclusion key 1.5.x stored.
        const val SURROUND_A_WORD_HASH = "4e5464d16f024bd533c31046ad855e999adbf2aa7975213e7c454eaf6578d8b0"
    }

    private class FakeTipSource(
        private val loadTipsResult: TipSourceLoadResult,
        private val loadTipsConditionalResult: TipSourceLoadResult = loadTipsResult
    ) : TipSourceService {
        var loadTipsCalls = 0
            private set

        var loadTipsConditionalCalls = 0
            private set

        override fun loadTips(): TipSourceLoadResult {
            loadTipsCalls += 1
            return loadTipsResult
        }

        override fun loadTipsConditional(metadata: TipMetadata): TipSourceLoadResult {
            loadTipsConditionalCalls += 1
            return loadTipsConditionalResult
        }
    }
}
