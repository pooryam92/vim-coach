package com.github.pooryam92.vimcoach.features.tips.unit.ideavimrc

import com.github.pooryam92.vimcoach.features.tips.application.ideavimrc.IdeaVimPluginFindVimRc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

class IdeaVimPluginFindVimRcUnitTest {

    private fun finder(
        home: String? = "/home/user",
        xdgConfigHome: String? = null,
        existingPaths: Set<Path> = emptySet()
    ) = IdeaVimPluginFindVimRc(
        home = { home },
        xdgConfigHome = { xdgConfigHome },
        fileExists = { it in existingPaths }
    )

    @Test
    fun returnsNullWhenHomeIsUnavailable() {
        assertNull(finder(home = null).findVimRc())
    }

    @Test
    fun returnsDotIdeaVimRcWhenItExists() {
        val expected = Path.of("/home/user/.ideavimrc")
        assertEquals(expected, finder(existingPaths = setOf(expected)).findVimRc())
    }

    @Test
    fun returnsUnderscoreIdeaVimRcWhenDotVariantAbsent() {
        val expected = Path.of("/home/user/_ideavimrc")
        assertEquals(expected, finder(existingPaths = setOf(expected)).findVimRc())
    }

    @Test
    fun prefersDotIdeaVimRcOverUnderscoreVariant() {
        val dot = Path.of("/home/user/.ideavimrc")
        val underscore = Path.of("/home/user/_ideavimrc")
        assertEquals(dot, finder(existingPaths = setOf(dot, underscore)).findVimRc())
    }

    @Test
    fun returnsDefaultXdgPathWhenNoDotFileExists() {
        val expected = Path.of("/home/user/.config/ideavim/ideavimrc")
        assertEquals(expected, finder(existingPaths = setOf(expected)).findVimRc())
    }

    @Test
    fun returnsNullWhenNoFileExistsAnywhere() {
        assertNull(finder().findVimRc())
    }

    @Test
    fun usesCustomXdgConfigHomeWhenSet() {
        val expected = Path.of("/custom/config/ideavim/ideavimrc")
        assertEquals(
            expected,
            finder(xdgConfigHome = "/custom/config", existingPaths = setOf(expected)).findVimRc()
        )
    }

    @Test
    fun expandsTildeSlashInXdgConfigHome() {
        val expected = Path.of("/home/user/myconfig/ideavim/ideavimrc")
        assertEquals(
            expected,
            finder(xdgConfigHome = "~/myconfig", existingPaths = setOf(expected)).findVimRc()
        )
    }

    @Test
    fun expandsTildeBackslashInXdgConfigHome() {
        val expected = Path.of("/home/user\\myconfig/ideavim/ideavimrc")
        assertEquals(
            expected,
            finder(xdgConfigHome = "~\\myconfig", existingPaths = setOf(expected)).findVimRc()
        )
    }

    @Test
    fun doesNotExpandBareTildeInXdgConfigHome() {
        val expected = Path.of("~").resolve("ideavim/ideavimrc")
        assertEquals(
            expected,
            finder(xdgConfigHome = "~", existingPaths = setOf(expected)).findVimRc()
        )
    }

    // XDG edge case: empty string must fall back to the default path (mirrors IdeaVim's "" == null check)
    @Test
    fun returnsDefaultXdgPathWhenXdgConfigHomeIsEmpty() {
        val expected = Path.of("/home/user/.config/ideavim/ideavimrc")
        assertEquals(expected, finder(xdgConfigHome = "", existingPaths = setOf(expected)).findVimRc())
    }

    // macOS: /Users/<name> — path semantics are identical to Linux
    @Test
    fun findsDotFileWithMacOsHomePath() {
        val expected = Path.of("/Users/john/.ideavimrc")
        assertEquals(expected, finder(home = "/Users/john", existingPaths = setOf(expected)).findVimRc())
    }

    // Windows: home = C:\Users\john — expected is built with the same Path API the production code uses,
    // so the equality check is valid regardless of which OS runs this test.
    @Test
    fun findsDotFileWithWindowsHomePath() {
        val home = "C:\\Users\\john"
        val expected = Path.of(home, ".ideavimrc")
        assertEquals(expected, finder(home = home, existingPaths = setOf(expected)).findVimRc())
    }

    @Test
    fun returnsDefaultXdgPathWithWindowsHomePath() {
        val home = "C:\\Users\\john"
        val expected = Path.of(home, ".config").resolve("ideavim/ideavimrc")
        assertEquals(expected, finder(home = home, existingPaths = setOf(expected)).findVimRc())
    }

    @Test
    fun expandsTildeBackslashWithWindowsHomePath() {
        // ~\AppData\Roaming is a realistic XDG_CONFIG_HOME value on Windows
        val home = "C:\\Users\\john"
        val expanded = home + "\\AppData\\Roaming"
        val expected = Path.of(expanded).resolve("ideavim/ideavimrc")
        assertEquals(
            expected,
            finder(home = home, xdgConfigHome = "~\\AppData\\Roaming", existingPaths = setOf(expected)).findVimRc()
        )
    }
}
