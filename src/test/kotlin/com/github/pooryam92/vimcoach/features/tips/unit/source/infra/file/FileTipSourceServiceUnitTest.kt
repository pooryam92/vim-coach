package com.github.pooryam92.vimcoach.features.tips.unit.source.infra.file

import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.github.pooryam92.vimcoach.features.tips.source.infra.file.FileTipSourceServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class FileTipSourceServiceUnitTest {

    @Test
    fun loadsTipsFromJsonFile() {
        val tipsFile = createTipsFile(
            """
            {
              "tips": [
                {"summary":"summary-1","details":["details-1"]},
                {"summary":"summary-2","details":["details-2"]}
              ]
            }
            """.trimIndent()
        )
        val service = FileTipSourceServiceImpl { tipsFile }

        val result = service.loadTips()

        assertTrue(result is TipSourceLoadResult.Success)
        result as TipSourceLoadResult.Success
        assertEquals(2, result.tips.size)
        assertEquals("summary-1", result.tips[0].summary)
    }

    @Test
    fun returnsFailureWhenFileDoesNotExist() {
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
