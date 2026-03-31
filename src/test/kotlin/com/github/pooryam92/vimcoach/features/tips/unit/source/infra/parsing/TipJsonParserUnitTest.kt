package com.github.pooryam92.vimcoach.features.tips.unit.source.infra.parsing

import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import com.github.pooryam92.vimcoach.features.tips.source.infra.parsing.TipJsonParser
import org.junit.Assert.assertEquals
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
