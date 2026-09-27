package com.github.pooryam92.vimcoach.features.tips.unit.loading.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.TipKeySpan
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
                {"summary":"  summary-1  ","details":["details-1"],"category":[" motions ","motions",""]},
                {"summary":"  ","details":["details-2"]},
                {"summary":"summary-3","details":["details-3"]},
                {"summary":"summary-4","details":["  "]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                  "summary":"surround",
                  "details":["edit surroundings"],
                  "config":["  Plug 'tpope/vim-surround'  ", "", "Plug 'tpope/vim-surround'"]
                }
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                  "summary":"surround",
                  "details":["edit surroundings"],
                  "config":{"name":"  vim-surround  ","lines":["  Plug 'tpope/vim-surround'  "]}
                }
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                  "summary":"line numbers",
                  "details":["show line numbers"],
                  "config":{"name":"   ","lines":["set number"]}
                }
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                  "summary":"line numbers",
                  "details":["show line numbers"],
                  "config":{"name":"Install x","lines":["  ", ""]}
                }
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertNull(tips[0].config)
    }

    @Test
    fun parseTipsJsonDefaultsConfigToNullWhenAbsent() {
        val json = """
            {
              "tips": [
                {"summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertNull(tips[0].config)
    }

    @Test
    fun parseTipsJsonKeepsFirstTipWhenSummariesCollideAfterTrimming() {
        val json = """
            {
              "tips": [
                {"summary":"  jump  ", "details":["first"]},
                {"summary":"jump", "details":["second"]},
                {"summary":"other", "details":["third"]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(2, tips.size)
        assertEquals("jump", tips[0].summary)
        assertEquals(listOf("first"), tips[0].details)
        assertEquals("other", tips[1].summary)
    }

    @Test
    fun parseTipsJsonIgnoresLegacyMnemonicField() {
        val json = """
            {
              "tips": [
                {"summary":"Change inner word ciw", "details":["ciw replaces the word"], "mnemonic":"change inner word"}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertEquals("Change inner word ciw", tips[0].summary)
        assertEquals(listOf("ciw replaces the word"), tips[0].details)
    }

    @Test
    fun parseTipsJsonDefaultsAdvancedToFalseWhenAbsent() {
        val json = """
            {
              "tips": [
                {"summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertFalse(tips[0].advanced)
    }

    @Test
    fun parseTipsJsonReadsAdvancedFlag() {
        val json = """
            {
              "tips": [
                {"summary":"paste last search", "details":["Ctrl-r /"], "advanced":true}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                {"summary":"number", "details":["d1"], "advanced":1},
                {"summary":"object", "details":["d2"], "advanced":{"nested":true}},
                {"summary":"array", "details":["d3"], "advanced":[true]},
                {"summary":"null", "details":["d4"], "advanced":null},
                {"summary":"string", "details":["d5"], "advanced":"true"},
                {"summary":"boolean", "details":["d6"], "advanced":true}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                {"summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertNull(tips[0].mode)
    }

    @Test
    fun parseTipsJsonReadsKnownModes() {
        val json = """
            {
              "tips": [
                {"summary":"insert paste", "details":["Ctrl-r"], "mode":"insert"},
                {"summary":"visual swap", "details":["o"], "mode":"visual"},
                {"summary":"cmdline paste", "details":["Ctrl-r"], "mode":"command"}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

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
                {"summary":"unknown", "details":["d1"], "mode":"normal"},
                {"summary":"future", "details":["d2"], "mode":"operator-pending"},
                {"summary":"number", "details":["d3"], "mode":7},
                {"summary":"object", "details":["d4"], "mode":{"nested":true}},
                {"summary":"array", "details":["d5"], "mode":["insert"]},
                {"summary":"blank", "details":["d6"], "mode":"  "},
                {"summary":"valid", "details":["d7"], "mode":"insert"}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(7, tips.size)
        for (i in 0..5) {
            assertNull("tip $i keeps no mode", tips[i].mode)
        }
        assertEquals("insert", tips[6].mode)
    }

    @Test
    fun parseTipsJsonDefaultsKeysToEmptyWhenAbsent() {
        val tips = parse("""{"tips":[{"summary":"jump","details":["use %"]}]}""")

        assertEquals(emptyList<TipKeySpan>(), tips.single().keys)
    }

    @Test
    fun parseTipsJsonReadsKeySpansForSummaryAndDetails() {
        val json = """
            {"tips":[{"summary":"Delete lines dd","details":["x deletes a char","3dd deletes three"],
                      "keys":[[0,13,15],[2,0,3]]}]}
        """.trimIndent()

        val tip = parse(json).single()

        assertEquals(listOf(TipKeySpan(0, 13, 15), TipKeySpan(2, 0, 3)), tip.keys)
    }

    // A malformed `keys` entry must not cost the tip (or the file) anything beyond that entry.
    @Test
    fun parseTipsJsonDropsMalformedOrOutOfRangeKeysKeepingAllTips() {
        val json = """
            {
              "tips": [
                {"summary":"object", "details":["dd"], "keys":{"line":0}},
                {"summary":"string", "details":["dd"], "keys":"dd"},
                {"summary":"mixed", "details":["dd now"], "keys":[[1,0,2],[1,0],["1",0,2],[1,0.5,2],null,[1,3,6]]},
                {"summary":"range", "details":["dd"], "keys":[[1,0,3],[2,0,1],[1,2,2],[1,-1,1],[1,0,2]]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(4, tips.size)
        assertEquals(emptyList<TipKeySpan>(), tips[0].keys)
        assertEquals(emptyList<TipKeySpan>(), tips[1].keys)
        assertEquals(listOf(TipKeySpan(1, 0, 2), TipKeySpan(1, 3, 6)), tips[2].keys)
        assertEquals(listOf(TipKeySpan(1, 0, 2)), tips[3].keys)
    }

    // Offsets index the text as published; once normalization shifts a line they could point at
    // the wrong characters, so the tip is shown unstyled instead.
    @Test
    fun parseTipsJsonDropsKeysWhenNormalizationShiftsTheText() {
        val json = """
            {
              "tips": [
                {"summary":"padded", "details":["  dd deletes"], "keys":[[1,2,4]]},
                {"summary":"blank", "details":["", "dd deletes"], "keys":[[2,0,2]]},
                {"summary":"clean", "details":["dd deletes"], "keys":[[1,0,2]]}
              ]
            }
        """.trimIndent()

        val tips = parse(json)

        assertEquals(emptyList<TipKeySpan>(), tips[0].keys)
        assertEquals(emptyList<TipKeySpan>(), tips[1].keys)
        assertEquals(listOf(TipKeySpan(1, 0, 2)), tips[2].keys)
    }

    @Test
    fun parseTipsJsonIgnoresUnknownFields() {
        val json = """
            {
              "tips": [
                {"summary":"jump", "details":["use %"], "someFutureField":{"nested":42}}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertEquals("jump", tips[0].summary)
        assertFalse(tips[0].advanced)
    }

    @Test
    fun parseTipsJsonReturnsEmptyWhenTipsFieldIsMissing() {
        val json = """
            {
              "movement": [
                {"summary":"jump", "details":["use %"]}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(emptyList<VimTip>(), tips)
    }

    private fun parse(json: String) =
        TipJsonParser.parseTipsJson(ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
}
