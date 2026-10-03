package com.github.pooryam92.vimcoach.features.tips.testsupport

import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotationRepository
import com.github.pooryam92.vimcoach.features.tips.persistence.TipRotationRepositoryImpl
import com.github.pooryam92.vimcoach.features.tips.persistence.store.PersistentTipRotationStore

fun inMemoryTipRotation(): TipRotationRepository = TipRotationRepositoryImpl(PersistentTipRotationStore())
