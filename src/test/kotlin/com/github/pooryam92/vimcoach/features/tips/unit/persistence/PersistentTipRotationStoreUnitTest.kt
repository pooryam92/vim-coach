package com.github.pooryam92.vimcoach.features.tips.unit.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentTipRotationStore
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.util.xmlb.XmlSerializer
import org.junit.Assert.assertEquals
import org.junit.Test

class PersistentTipRotationStoreUnitTest {

    @Test
    fun progressSurvivesASaveAndLoad() {
        val saved = PersistentTipRotationStore()
        saved.updateProgress { PersistentTipRotationStore.State(mapOf("a" to 2, "b" to 1), lastShownKey = "a") }

        val loaded = PersistentTipRotationStore()
        loaded.loadState(saveAndReload(saved.state))

        assertEquals(saved.state, loaded.state)
    }

    @Test
    fun rotationIsStoredPerMachine() {
        val storage = PersistentTipRotationStore::class.java.getAnnotation(State::class.java).storages.single()

        assertEquals(RoamingType.DISABLED, storage.roamingType)
    }

    private fun saveAndReload(state: PersistentTipRotationStore.State): PersistentTipRotationStore.State {
        val serialized = XmlSerializer.serialize(state)
        return XmlSerializer.deserialize(serialized, PersistentTipRotationStore.State::class.java)
    }
}
