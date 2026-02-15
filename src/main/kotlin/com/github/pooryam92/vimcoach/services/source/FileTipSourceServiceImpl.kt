package com.github.pooryam92.vimcoach.services.source

import com.github.pooryam92.vimcoach.config.VimTipConfig
import com.github.pooryam92.vimcoach.services.TipJsonParser
import java.nio.file.Files
import java.nio.file.Path

class FileTipSourceServiceImpl(
    private val filePathProvider: () -> Path = { resolveConfiguredPath() }
) : FileTipSourceService {
    override fun loadTips(): TipSourceLoadResult {
        val filePath = filePathProvider.invoke()
        return try {
            Files.newInputStream(filePath).use { stream ->
                val tips = TipJsonParser.parseTipsJson(stream)
                if (tips.isEmpty()) {
                    TipSourceLoadResult.Empty
                } else {
                    TipSourceLoadResult.Success(tips)
                }
            }
        } catch (e: Exception) {
            TipSourceLoadResult.Failure("Failed to load tips file '$filePath': ${e.message}", e)
        }
    }

    companion object {
        private fun resolveConfiguredPath(): Path {
            val configuredPath = System.getProperty(VimTipConfig.TIPS_FILE_PATH_PROPERTY)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException(
                    "System property '${VimTipConfig.TIPS_FILE_PATH_PROPERTY}' is required in file mode."
                )
            return Path.of(configuredPath)
        }
    }
}
