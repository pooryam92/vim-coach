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
    fun parseTipsJsonReadsAndTrimsMnemonic() {
        val json = """
            {
              "tips": [
                {"summary":"Change inner word ciw", "details":["ciw replaces the word"], "mnemonic":"  change inner word  "}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertEquals("change inner word", tips[0].mnemonic)
    }

    @Test
    fun parseTipsJsonTreatsBlankMnemonicAsNull() {
        val json = """
            {
              "tips": [
                {"summary":"jump", "details":["use %"], "mnemonic":"   "}
              ]
            }
        """.trimIndent()

        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        assertEquals(1, tips.size)
        assertNull(tips[0].mnemonic)
    }

    @Test
    fun parseTipsJsonDefaultsMnemonicToNullWhenAbsent() {
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
        assertNull(tips[0].mnemonic)
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
}
