package com.github.pooryam92.vimcoach.services

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.ByteArrayInputStream

class RemoteTipSourceServiceImplTest : BasePlatformTestCase() {

    fun testParseTipsJsonFiltersInvalidEntries() {
        // Arrange
        val json = """
            [
              {"summary":"  summary-1  ","details":"details-1","category":"  category-1  "},
              {"summary":"  ","details":"details-2","category":"category-2"},
              {"summary":"summary-3","details":"  ","category":"category-3"},
              {"summary":"summary-4","details":"details-4","category":""}
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
        assertEquals("category-1", tips[0].category)
        assertEquals("summary-4", tips[1].summary)
        assertEquals("details-4", tips[1].details)
        assertEquals(null, tips[1].category)
    }
}
