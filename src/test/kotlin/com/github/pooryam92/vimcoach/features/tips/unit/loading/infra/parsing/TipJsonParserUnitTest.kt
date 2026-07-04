package com.github.pooryam92.vimcoach.features.tips.unit.loading.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.application.loading.infra.parsing.TipJsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
