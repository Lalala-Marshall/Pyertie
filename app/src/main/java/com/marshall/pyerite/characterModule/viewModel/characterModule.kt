package com.marshall.pyerite.characterModule.viewModel

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterModule = module {
    singleOf(::CharacterRepository)
    viewModelOf(::CharacterViewModel)
}
