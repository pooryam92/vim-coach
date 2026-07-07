package com.github.pooryam92.vimcoach.features.tips.application.notifications

import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Appends a maintainer note about a tip to an append-only markdown file, so a tip that needs
 * fixing can be flagged from inside the running IDE and picked up later.
 *
 * This is a **dev-only** aid: [fromEnvironment] returns null unless the [NOTES_FILE_PROPERTY]
 * system property is set (the dev `runIde` tasks set it to a file in the repo). A released build
 * never sets it, so nothing here runs and no file is ever written in production.
 */
class RecordTipNote(private val file: Path) {

    /** Returns the file the note was appended to, so callers can confirm where it landed. */
    fun record(tip: VimTip, note: String, at: LocalDateTime = LocalDateTime.now()): Path {
        file.parent?.let(Files::createDirectories)
        Files.write(
            file,
            entry(tip, note.trim(), at).toByteArray(Charsets.UTF_8),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
        )
        return file
    }

    private fun entry(tip: VimTip, note: String, at: LocalDateTime): String {
        val summary = tip.summary.trim().ifEmpty { NO_SUMMARY }
        return buildString {
            append('\n')
            append("## ").append(TIMESTAMP.format(at)).append(" — ").append(summary).append('\n')
            append("- **Hash:** `").append(TipHash.fromTip(tip).value).append("`\n")
            append("- **Note:** ").append(note).append('\n')
        }
    }

    companion object {
        const val NOTES_FILE_PROPERTY = "vimcoach.tip.notes.file"

        private const val NO_SUMMARY = "(no summary)"
        private val TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        /** A recorder when the dev-only notes file is configured, otherwise null (production). */
        fun fromEnvironment(): RecordTipNote? =
            System.getProperty(NOTES_FILE_PROPERTY)
                ?.takeIf(String::isNotBlank)
                ?.let { RecordTipNote(Path.of(it)) }
    }
}
