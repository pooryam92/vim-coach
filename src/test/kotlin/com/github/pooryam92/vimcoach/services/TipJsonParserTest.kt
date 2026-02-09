package com.github.pooryam92.vimcoach.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.ByteArrayInputStream

class TipJsonParserTest : BasePlatformTestCase() {

    fun testParseTipsJsonFiltersInvalidEntries() {
        // Arrange
        val json = """
            [
              {"summary":"  summary-1  ","details":"details-1"},
              {"summary":"  ","details":"details-2"},
              {"summary":"summary-3","details":"details-3"},
              {"summary":"summary-4","details":"  "}
            ]
        """.trimIndent()

        // Act
        val tips = TipJsonParser.parseTipsJson(
            ByteArrayInputStream(json.toByteArray(Charsets.UTF_8))
        )

        // Assert
        assertEquals(2, tips.size)
        assertEquals("summary-1", tips[0].summary)
        assertEquals("details-1", tips[0].details)
        assertEquals("summary-3", tips[1].summary)
        assertEquals("details-3", tips[1].details)
    }
}
