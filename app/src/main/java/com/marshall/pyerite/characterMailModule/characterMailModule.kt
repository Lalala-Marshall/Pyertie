package com.marshall.pyerite.characterMailModule

import com.marshall.pyerite.characterMailModule.data.CharacterMailLoader
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailDetailViewModel
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailRepository
import com.marshall.pyerite.characterMailModule.viewModel.CharacterMailViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterMailModule = module {
    singleOf(::CharacterMailLoader)
    singleOf(::CharacterMailRepository)
    viewModelOf(::CharacterMailViewModel)
    viewModelOf(::CharacterMailDetailViewModel)
}
