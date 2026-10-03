package com.github.pooryam92.vimcoach.features.tips.unit.loading.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.application.loading.infra.parsing.TipJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class TipJsonParserUnitTest {

    @Test
    fun parseTipsJsonFiltersInvalidEntries() {
        val json = """
            {
              "tips": [
                {"id":"id-1","summary":"  summary-1  ","details":["details-1"],"category":[" motions ","motions",""]},
                {"id":"id-2","summary":"  ","details":["details-2"]},
                {"id":"id-3","summary":"summary-3","details":["details-3"]},
                {"id":"id-4","summary":"summary-4","details":["  "]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(2, tips.size)
        assertEquals("summary-1", tips[0].summary)
        assertEquals(listOf("details-1"), tips[0].details)
        assertEquals(listOf("motions"), tips[0].category)
        assertEquals("summary-3", tips[1].summary)
        assertEquals(listOf("details-3"), tips[1].details)
        assertEquals(emptyList<String>(), tips[1].category)
    }

    @Test
    fun parseTipsJsonReadsLegacyArrayConfigPreservingOrderAndDuplicates() {
        val json = """
            {
              "tips": [
                {
                  "id":"id-5",
                  "summary":"surround",
                  "details":["edit surroundings"],
                  "config":["  Plug 'tpope/vim-surround'  ", "", "Plug 'tpope/vim-surround'"]
                }
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].config?.name)
        assertEquals(
            listOf("Plug 'tpope/vim-surround'", "Plug 'tpope/vim-surround'"),
            tips[0].config?.lines
        )
    }

    @Test
    fun parseTipsJsonReadsNamedConfigObject() {
        val json = """
            {
              "tips": [
                {
                  "id":"id-6",
                  "summary":"surround",
                  "details":["edit surroundings"],
                  "config":{"name":"  vim-surround  ","lines":["  Plug 'tpope/vim-surround'  "]}
                }
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertEquals("vim-surround", tips[0].config?.name)
        assertEquals(listOf("Plug 'tpope/vim-surround'"), tips[0].config?.lines)
    }

    @Test
    fun parseTipsJsonTreatsBlankConfigNameAsNull() {
        val json = """
            {
              "tips": [
                {
                  "id":"id-7",
                  "summary":"line numbers",
                  "details":["show line numbers"],
                  "config":{"name":"   ","lines":["set number"]}
                }
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].config?.name)
        assertEquals(listOf("set number"), tips[0].config?.lines)
    }

    @Test
    fun parseTipsJsonDropsConfigWhenLinesAreAllBlank() {
        val json = """
            {
              "tips": [
                {
                  "id":"id-8",
                  "summary":"line numbers",
                  "details":["show line numbers"],
                  "config":{"name":"Install x","lines":["  ", ""]}
                }
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].config)
    }

    @Test
    fun parseTipsJsonDefaultsConfigToNullWhenAbsent() {
        val json = """
            {
              "tips": [
                {"id":"id-9","summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].config)
    }

    @Test
    fun parseTipsJsonReadsAndTrimsId() {
        val tips = parse("""{"tips":[{"id":"  14ec7dc7  ","summary":"jump","details":["use %"]}]}""")

        assertEquals(listOf("14ec7dc7"), tips.map(VimTip::id))
    }

    @Test
    fun parseTipsJsonDropsTipsWithMissingOrBlankId() {
        val json = """
            {
              "tips": [
                {"summary":"missing", "details":["d1"]},
                {"id":"   ", "summary":"blank", "details":["d2"]},
                {"id":"id-3", "summary":"kept", "details":["d3"]}
              ]
            }
        """.trimIndent()

        assertEquals(listOf("kept"), parse(json).map(VimTip::summary))
    }

    // A malformed id must drop only its own tip: Gson would otherwise coerce a number into an id
    // or abort the whole tips array on an object or array.
    @Test
    fun parseTipsJsonDropsOnlyTheTipsWithNonStringIds() {
        val json = """
            {
              "tips": [
                {"id":7, "summary":"number", "details":["d1"]},
                {"id":{"nested":true}, "summary":"object", "details":["d2"]},
                {"id":["id-3"], "summary":"array", "details":["d3"]},
                {"id":true, "summary":"boolean", "details":["d4"]},
                {"id":null, "summary":"null", "details":["d5"]},
                {"id":"id-6", "summary":"kept", "details":["d6"]}
              ]
            }
        """.trimIndent()

        assertEquals(listOf("kept"), parse(json).map(VimTip::summary))
    }

    @Test
    fun parseTipsJsonKeepsFirstTipWhenIdsCollideAfterTrimming() {
        val json = """
            {
              "tips": [
                {"id":" same ", "summary":"first", "details":["d1"]},
                {"id":"same", "summary":"second", "details":["d2"]},
                {"id":"other", "summary":"third", "details":["d3"]}
              ]
            }
        """.trimIndent()

        assertEquals(listOf("first", "third"), parse(json).map(VimTip::summary))
    }

    @Test
    fun parseTipsJsonKeepsTipsSharingASummaryWhenIdsDiffer() {
        val json = """
            {
              "tips": [
                {"id":"id-1", "summary":"jump", "details":["first"]},
                {"id":"id-2", "summary":"jump", "details":["second"]}
              ]
            }
        """.trimIndent()

        assertEquals(listOf("id-1", "id-2"), parse(json).map(VimTip::id))
    }

    @Test
    fun parseTipsJsonReadsAndTrimsMnemonic() {
        val json = """
            {
              "tips": [
                {"id":"id-13","summary":"Change inner word ciw", "details":["ciw replaces the word"], "mnemonic":"  change inner word  "}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertEquals("change inner word", tips[0].mnemonic)
    }

    @Test
    fun parseTipsJsonTreatsBlankMnemonicAsNull() {
        val json = """
            {
              "tips": [
                {"id":"id-14","summary":"jump", "details":["use %"], "mnemonic":"   "}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].mnemonic)
    }

    @Test
    fun parseTipsJsonDefaultsMnemonicToNullWhenAbsent() {
        val json = """
            {
              "tips": [
                {"id":"id-15","summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].mnemonic)
    }

    @Test
    fun parseTipsJsonDefaultsAdvancedToFalseWhenAbsent() {
        val json = """
            {
              "tips": [
                {"id":"id-16","summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertFalse(tips[0].advanced)
    }

    @Test
    fun parseTipsJsonReadsAdvancedFlag() {
        val json = """
            {
              "tips": [
                {"id":"id-17","summary":"paste last search", "details":["Ctrl-r /"], "advanced":true}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertTrue(tips[0].advanced)
    }

    // A malformed `advanced` must not abort the parse: user-supplied files (file mode, custom
    // remote URL) rely on the documented leniency, and one bad tip must not blank out all tips.
    @Test
    fun parseTipsJsonIgnoresNonBooleanAdvancedValuesKeepingAllTips() {
        val json = """
            {
              "tips": [
                {"id":"id-18","summary":"number", "details":["d1"], "advanced":1},
                {"id":"id-19","summary":"object", "details":["d2"], "advanced":{"nested":true}},
                {"id":"id-20","summary":"array", "details":["d3"], "advanced":[true]},
                {"id":"id-21","summary":"null", "details":["d4"], "advanced":null},
                {"id":"id-22","summary":"string", "details":["d5"], "advanced":"true"},
                {"id":"id-23","summary":"boolean", "details":["d6"], "advanced":true}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(6, tips.size)
        assertFalse(tips[0].advanced)
        assertFalse(tips[1].advanced)
        assertFalse(tips[2].advanced)
        assertFalse(tips[3].advanced)
        assertFalse("only a JSON boolean marks a tip advanced", tips[4].advanced)
        assertTrue(tips[5].advanced)
    }

    @Test
    fun parseTipsJsonDefaultsModeToNullWhenAbsent() {
        val json = """
            {
              "tips": [
                {"id":"id-24","summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertNull(tips[0].mode)
    }

    @Test
    fun parseTipsJsonReadsKnownModes() {
        val json = """
            {
              "tips": [
                {"id":"id-25","summary":"insert paste", "details":["Ctrl-r"], "mode":"insert"},
                {"id":"id-26","summary":"visual swap", "details":["o"], "mode":"visual"},
                {"id":"id-27","summary":"cmdline paste", "details":["Ctrl-r"], "mode":"command"}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(3, tips.size)
        assertEquals("insert", tips[0].mode)
        assertEquals("visual", tips[1].mode)
        assertEquals("command", tips[2].mode)
    }

    // A mode this plugin version does not know (or a malformed value) must fall back to no label
    // without aborting the parse — same forward-compat guarantee the `advanced` field relies on.
    @Test
    fun parseTipsJsonDropsUnknownOrMalformedModesKeepingAllTips() {
        val json = """
            {
              "tips": [
                {"id":"id-28","summary":"unknown", "details":["d1"], "mode":"normal"},
                {"id":"id-29","summary":"future", "details":["d2"], "mode":"operator-pending"},
                {"id":"id-30","summary":"number", "details":["d3"], "mode":7},
                {"id":"id-31","summary":"object", "details":["d4"], "mode":{"nested":true}},
                {"id":"id-32","summary":"array", "details":["d5"], "mode":["insert"]},
                {"id":"id-33","summary":"blank", "details":["d6"], "mode":"  "},
                {"id":"id-34","summary":"valid", "details":["d7"], "mode":"insert"}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(7, tips.size)
        for (i in 0..5) {
            assertNull("tip $i keeps no mode", tips[i].mode)
        }
        assertEquals("insert", tips[6].mode)
    }

    @Test
    fun parseTipsJsonIgnoresUnknownFields() {
        val json = """
            {
              "tips": [
                {"id":"id-35","summary":"jump", "details":["use %"], "someFutureField":{"nested":42}}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(1, tips.size)
        assertEquals("jump", tips[0].summary)
        assertFalse(tips[0].advanced)
    }

    @Test
    fun parseTipsJsonReturnsEmptyWhenTipsFieldIsMissing() {
        val json = """
            {
              "movement": [
                {"id":"id-36","summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(emptyList<VimTip>(), tips)
    }

    private fun parse(json: String): List<VimTip> =
        TipJsonParser.parseTipsJson(ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
}
