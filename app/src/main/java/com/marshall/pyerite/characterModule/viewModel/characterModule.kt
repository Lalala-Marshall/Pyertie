package com.marshall.pyerite.characterModule.viewModel

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterModule = module {
    single {
        CharacterRepository(
            tokenManager = get(),
            selectionStore = get(),
        )
    }
    viewModelOf(::CharacterViewModel)
}
