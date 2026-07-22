package com.marshall.pyerite.charactersListModule.viewModel

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val charactersListModule = module {
    single {
        CharacterRepository(
            tokenManager = get(),
            selectionStore = get(),
            orderStore = get(),
        )
    }
    viewModelOf(::CharacterViewModel)
}
