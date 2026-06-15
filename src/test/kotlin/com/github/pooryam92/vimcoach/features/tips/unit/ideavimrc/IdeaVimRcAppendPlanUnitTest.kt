package com.github.pooryam92.vimcoach.features.tips.unit.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.IdeaVimRcAppendPlan
import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.IdeaVimRcAppendPlan.Plan
import org.junit.Assert.assertEquals
import org.junit.Test

class IdeaVimRcAppendPlanUnitTest {

    @Test
    fun returnsEmptyWhenNoConfigLines() {
        assertEquals(Plan.Empty, IdeaVimRcAppendPlan.of(existingText = "", configLines = emptyList()))
    }

    @Test
    fun returnsEmptyWhenAllConfigLinesAreBlank() {
        assertEquals(Plan.Empty, IdeaVimRcAppendPlan.of(existingText = "set x", configLines = listOf("", "   ")))
    }

    @Test
    fun appendsSingleLineToEmptyFileAtLineZero() {
        val plan = IdeaVimRcAppendPlan.of(existingText = "", configLines = listOf("set surround")) as Plan.Append
        assertEquals("set surround\n", plan.insertText)
        assertEquals(0, plan.startLine)
        assertEquals(1, plan.addedCount)
    }

    @Test
    fun appendsAfterTrailingNewlineWithoutAddingBlankLine() {
        val plan = IdeaVimRcAppendPlan.of(existingText = "set a\n", configLines = listOf("set b")) as Plan.Append
        assertEquals("set b\n", plan.insertText)
        // "set a\n" is two document lines (line 0 = "set a", line 1 = ""), so the new line lands on line 1.
        assertEquals(1, plan.startLine)
        assertEquals(1, plan.addedCount)
    }

    @Test
    fun insertsLeadingNewlineWhenFileDoesNotEndWithNewline() {
        val plan = IdeaVimRcAppendPlan.of(existingText = "set a", configLines = listOf("set b")) as Plan.Append
        assertEquals("\nset b\n", plan.insertText)
        // "set a" is a single line (line 0); the appended line becomes line 1.
        assertEquals(1, plan.startLine)
        assertEquals(1, plan.addedCount)
    }

    @Test
    fun appendsMultipleLines() {
        val plan = IdeaVimRcAppendPlan.of(existingText = "", configLines = listOf("set a", "set b")) as Plan.Append
        assertEquals("set a\nset b\n", plan.insertText)
        assertEquals(0, plan.startLine)
        assertEquals(2, plan.addedCount)
    }

    @Test
    fun returnsAlreadyPresentWhenEveryLineExists() {
        assertEquals(
            Plan.AlreadyPresent,
            IdeaVimRcAppendPlan.of(existingText = "set a\nset b\n", configLines = listOf("set a", "set b"))
        )
    }

    @Test
    fun appendsOnlyTheLinesThatAreMissing() {
        val plan = IdeaVimRcAppendPlan.of(
            existingText = "set a\n",
            configLines = listOf("set a", "set b")
        ) as Plan.Append
        assertEquals("set b\n", plan.insertText)
        assertEquals(1, plan.addedCount)
    }

    @Test
    fun matchesExistingLinesIgnoringSurroundingWhitespace() {
        assertEquals(
            Plan.AlreadyPresent,
            IdeaVimRcAppendPlan.of(existingText = "   set a   \n", configLines = listOf("set a"))
        )
    }

    @Test
    fun trimsConfigLinesBeforeAppending() {
        val plan = IdeaVimRcAppendPlan.of(existingText = "", configLines = listOf("  set a  ")) as Plan.Append
        assertEquals("set a\n", plan.insertText)
    }

    @Test
    fun deduplicatesRepeatedConfigLines() {
        val plan = IdeaVimRcAppendPlan.of(
            existingText = "",
            configLines = listOf("set a", "set a", "set b")
        ) as Plan.Append
        assertEquals("set a\nset b\n", plan.insertText)
        assertEquals(2, plan.addedCount)
    }
}
