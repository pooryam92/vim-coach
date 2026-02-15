package com.github.pooryam92.vimcoach.services.source

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path

class FileTipSourceServiceTest : BasePlatformTestCase() {

    fun testLoadsTipsFromJsonFile() {
        val tipsFile = createTipsFile(
            """
            [
              {"summary":"summary-1","details":"details-1"},
              {"summary":"summary-2","details":"details-2"}
            ]
            """.trimIndent()
        )
        val service = FileTipSourceServiceImpl { tipsFile }

        val result = service.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        result as TipSourceLoadResult.Success
        assertEquals(2, result.tips.size)
        assertEquals("summary-1", result.tips[0].summary)
    }

    fun testReturnsFailureWhenFileDoesNotExist() {
        val missingFile = Path.of("/tmp/missing-vimcoach-tips.json")
        val service = FileTipSourceServiceImpl { missingFile }

        val result = service.loadTips()

        assertTrue(result is TipSourceLoadResult.Failure)
    }

    private fun createTipsFile(content: String): Path {
        val file = Files.createTempFile("vimcoach-tips-", ".json")
        Files.writeString(file, content)
        return file
    }
}
