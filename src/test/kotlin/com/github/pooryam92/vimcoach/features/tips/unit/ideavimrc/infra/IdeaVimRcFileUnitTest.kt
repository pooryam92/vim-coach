package com.github.pooryam92.vimcoach.features.tips.unit.ideavimrc.infra

import com.github.pooryam92.vimcoach.features.tips.ideavimrc.infra.IdeaVimRcFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

class IdeaVimRcFileUnitTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun findOrCreateCreatesDotIdeaVimRcWhenNoneExists() {
        val home = tempFolder.newFolder("home")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)

        val path = file.findOrCreate()

        assertTrue(path != null && path.exists())
        assertEquals(".ideavimrc", path!!.name)
    }

    @Test
    fun findReturnsExistingDotIdeaVimRc() {
        val home = tempFolder.newFolder("home")
        val existing = home.toPath().resolve(".ideavimrc")
        existing.writeText("set scrolloff=5\n")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)

        assertEquals(existing, file.find())
    }

    @Test
    fun appendAddsNewLinesAndSkipsOnesAlreadyPresent() {
        val home = tempFolder.newFolder("home")
        val path = home.toPath().resolve(".ideavimrc")
        path.writeText("set scrolloff=5\nPlug 'tpope/vim-surround'\n")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)

        val outcome = file.append(
            path,
            listOf("Plug 'tpope/vim-surround'", "Plug 'tpope/vim-commentary'")
        )

        assertEquals(listOf("Plug 'tpope/vim-commentary'"), outcome.addedLines)
        assertEquals(listOf("Plug 'tpope/vim-surround'"), outcome.alreadyPresentLines)
        assertTrue(outcome.addedSomething)
        // The two existing lines occupy lines 0 and 1, so the appended line lands on line 2.
        assertEquals(2, outcome.addedStartLine)
        val content = path.readText()
        assertTrue(content.contains("Plug 'tpope/vim-commentary'"))
        // surround is not duplicated
        assertEquals(1, Regex("vim-surround").findAll(content).count())
    }

    @Test
    fun appendReportsStartLineZeroForFreshlyCreatedFile() {
        val home = tempFolder.newFolder("home")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)
        val path = file.findOrCreate()!!

        val outcome = file.append(path, listOf("Plug 'tpope/vim-surround'"))

        assertEquals(0, outcome.addedStartLine)
    }

    @Test
    fun appendInsertsNewlineWhenFileLacksTrailingNewline() {
        val home = tempFolder.newFolder("home")
        val path = home.toPath().resolve(".ideavimrc")
        path.writeText("set scrolloff=5")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)

        file.append(path, listOf("Plug 'tpope/vim-surround'"))

        val lines = path.readText().lines().filter { it.isNotEmpty() }
        assertEquals(listOf("set scrolloff=5", "Plug 'tpope/vim-surround'"), lines)
    }

    @Test
    fun appendReportsNothingAddedWhenAllLinesAlreadyPresent() {
        val home = tempFolder.newFolder("home")
        val path = home.toPath().resolve(".ideavimrc")
        path.writeText("Plug 'tpope/vim-surround'\n")
        val file = IdeaVimRcFile(userHome = home.absolutePath, xdgConfigHome = null)

        val outcome = file.append(path, listOf("Plug 'tpope/vim-surround'"))

        assertFalse(outcome.addedSomething)
        assertEquals(listOf("Plug 'tpope/vim-surround'"), outcome.alreadyPresentLines)
        assertEquals(-1, outcome.addedStartLine)
    }
}
