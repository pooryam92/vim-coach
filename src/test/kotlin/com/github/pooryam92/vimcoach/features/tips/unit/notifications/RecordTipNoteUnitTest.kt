package com.github.pooryam92.vimcoach.features.tips.unit.notifications

import com.github.pooryam92.vimcoach.features.tips.application.notifications.RecordTipNote
import com.github.pooryam92.vimcoach.features.tips.domain.TipHash
import com.github.pooryam92.vimcoach.features.tips.domain.VimTip
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.time.LocalDateTime

class RecordTipNoteUnitTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val originalProperty: String? = System.getProperty(RecordTipNote.NOTES_FILE_PROPERTY)

    @After
    fun restoreProperty() {
        if (originalProperty == null) System.clearProperty(RecordTipNote.NOTES_FILE_PROPERTY)
        else System.setProperty(RecordTipNote.NOTES_FILE_PROPERTY, originalProperty)
    }

    @Test
    fun recordWritesSummaryHashAndNote() {
        val file = tempFolder.root.toPath().resolve("notes.md")
        val tip = VimTip(summary = "Move by word with w/b/e.")

        RecordTipNote(file).record(tip, "wording is ambiguous", at = at)

        val content = Files.readString(file)
        assertTrue(content.contains("## 2026-07-04T14:22:01 — Move by word with w/b/e."))
        assertTrue(content.contains("**Hash:** `${TipHash.fromTip(tip).value}`"))
        assertTrue(content.contains("**Note:** wording is ambiguous"))
    }

    @Test
    fun recordCreatesMissingParentDirectories() {
        val file = tempFolder.root.toPath().resolve("nested/dir/notes.md")

        RecordTipNote(file).record(VimTip(summary = "tip"), "note")

        assertTrue(Files.exists(file))
    }

    @Test
    fun recordAppendsWithoutOverwriting() {
        val file = tempFolder.root.toPath().resolve("notes.md")
        val recorder = RecordTipNote(file)

        recorder.record(VimTip(summary = "first tip"), "first note", at = at)
        recorder.record(VimTip(summary = "second tip"), "second note", at = at)

        val content = Files.readString(file)
        assertTrue(content.contains("first tip"))
        assertTrue(content.contains("first note"))
        assertTrue(content.contains("second tip"))
        assertTrue(content.contains("second note"))
    }

    @Test
    fun recordTrimsTheNote() {
        val file = tempFolder.root.toPath().resolve("notes.md")

        RecordTipNote(file).record(VimTip(summary = "tip"), "  spaced note  ")

        assertTrue(Files.readString(file).contains("**Note:** spaced note\n"))
    }

    @Test
    fun fromEnvironmentIsEnabledOnlyWhenPropertySet() {
        System.clearProperty(RecordTipNote.NOTES_FILE_PROPERTY)
        assertNull(RecordTipNote.fromEnvironment())

        System.setProperty(RecordTipNote.NOTES_FILE_PROPERTY, tempFolder.root.toPath().resolve("n.md").toString())
        assertNotNull(RecordTipNote.fromEnvironment())
    }

    @Test
    fun fromEnvironmentIgnoresBlankProperty() {
        System.setProperty(RecordTipNote.NOTES_FILE_PROPERTY, "   ")
        assertNull(RecordTipNote.fromEnvironment())
    }

    private val at = LocalDateTime.of(2026, 7, 4, 14, 22, 1)
}
