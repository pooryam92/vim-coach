package com.github.pooryam92.vimcoach.features.tips.unit.wiring

import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the optional IdeaVim descriptor against drifting away from Gradle's Marketplace dependency list.
 */
class IdeaVimPluginDependencyUnitTest {

    @Test
    fun ideaVimOptionalDescriptorDependencyIsDeclaredAsPlatformPlugin() {
        val optionalDependencyIds = pluginXmlOptionalDependencyIds()
        val platformPluginIds = platformPluginIds()

        assertTrue("plugin.xml must keep IdeaVIM as an optional dependency", "IdeaVIM" in optionalDependencyIds)
        assertTrue("gradle.properties must declare IdeaVIM in platformPlugins", "IdeaVIM" in platformPluginIds)
    }

    private fun pluginXmlOptionalDependencyIds(): Set<String> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(Path.of("src/main/resources/META-INF/plugin.xml").toFile())
        val depends = document.getElementsByTagName("depends")

        return (0 until depends.length).mapNotNull { index ->
            val element = depends.item(index) as? Element
            element
                ?.takeIf { it.getAttribute("optional") == "true" }
                ?.textContent
                ?.trim()
        }.toSet()
    }

    private fun platformPluginIds(): Set<String> {
        val properties = Properties()
        Files.newInputStream(Path.of("gradle.properties")).use(properties::load)

        return properties.getProperty("platformPlugins", "")
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map { it.substringBefore(':').substringBefore('@') }
            .toSet()
    }
}
