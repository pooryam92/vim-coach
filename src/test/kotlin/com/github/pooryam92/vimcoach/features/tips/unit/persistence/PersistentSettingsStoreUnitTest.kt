package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentSettingsStore
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
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
}
