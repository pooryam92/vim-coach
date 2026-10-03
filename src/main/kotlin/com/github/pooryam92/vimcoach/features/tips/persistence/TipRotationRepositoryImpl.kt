package com.github.pooryam92.vimcoach.features.tips.persistence

import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentTipRotationStore
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger

class TipRotationRepositoryImpl() : TipRotationRepository {
    private var injectedRotationStore: PersistentTipRotationStore? = null

    internal constructor(rotationStore: PersistentTipRotationStore) : this() {
        injectedRotationStore = rotationStore
    }

    override fun getProgress(): RotationProgress {
        val state = currentState()
        return RotationProgress(state.timesShown, state.lastShownKey)
    }

    override fun recordShown(key: String, count: Int, cachedKeys: Set<String>) {
        var prunedCount = 0
        rotationStore().updateProgress { state ->
            val retained = state.timesShown.filterKeys { it in cachedKeys }
            prunedCount = state.timesShown.size - retained.size
            state.copy(timesShown = retained + (key to count), lastShownKey = key)
        }
        logPruned(prunedCount)
    }

    private fun logPruned(count: Int) {
        if (count > 0) {
            logger.debug("Pruned $count rotation entries for tips no longer in the cache")
        }
    }

    private fun currentState(): PersistentTipRotationStore.State {
        return rotationStore().state
    }

    private fun rotationStore(): PersistentTipRotationStore {
        return injectedRotationStore ?: service()
    }

    private companion object {
        val logger = Logger.getInstance(TipRotationRepositoryImpl::class.java)
    }
}
