package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.xmlb.XmlSerializer
import org.junit.Assert.assertEquals
import org.junit.Test

class PersistentSettingsStoreUnitTest {

    @Test
    fun settingsAreStoredPerMachine() {
        val storage = PersistentSettingsStore::class.java.getAnnotation(State::class.java).storages.single()

        assertEquals(RoamingType.DISABLED, storage.roamingType)
    }

    @Test
    fun settingsKeepTheirFileName() {
        val storage = PersistentSettingsStore::class.java.getAnnotation(State::class.java).storages.single()

        assertEquals("vim-coach-settings.xml", storage.value)
    }

    // TODO(1.6.0 upgrade bridge)
    @Test
    fun exclusionsSavedBefore160LoadAsLegacyHashes() {
        val savedBy151 = JDOMUtil.load(
            """
            <State>
              <option name="hiddenTipHashes">
                <list>
                  <option value="legacy-hash" />
                </list>
              </option>
            </State>
            """.trimIndent()
        )

        val state = XmlSerializer.deserialize(savedBy151, PersistentSettingsStore.State::class.java)

        assertEquals(listOf("legacy-hash"), state.hiddenTipHashes)
    }
}
