package com.github.pooryam92.vimcoach.features.tips.source.infra.file

import com.github.pooryam92.vimcoach.features.tips.source.infra.config.VimTipConfig
import com.github.pooryam92.vimcoach.features.tips.source.infra.parsing.TipJsonParser
import com.github.pooryam92.vimcoach.features.tips.domain.TipMetadata
import com.github.pooryam92.vimcoach.features.tips.source.domain.TipSourceLoadResult
import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Files
import java.nio.file.Path

class FileTipSourceServiceImpl(
    private val filePathProvider: () -> Path = { resolveConfiguredPath() }
) : FileTipSourceService {
    override fun loadTips(): TipSourceLoadResult {
        val filePath = filePathProvider.invoke()
        logger.info("Loading Vim tips from file: $filePath")
        return try {
            Files.newInputStream(filePath).use { stream ->
                val tips = TipJsonParser.parseTipsJson(stream)
                if (tips.isEmpty()) {
                    logger.info("File tip source returned no valid tips from $filePath")
                    TipSourceLoadResult.Empty
                } else {
                    logger.info("Loaded ${tips.size} Vim tips from file source")
                    TipSourceLoadResult.Success(tips, TipMetadata())
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load Vim tips from file: $filePath", e)
            TipSourceLoadResult.Failure("Failed to load tips file '$filePath': ${e.message}", e)
        }
    }

    companion object {
        private val logger = Logger.getInstance(FileTipSourceServiceImpl::class.java)

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
