package com.marshall.pyerite.characterMasteryModule

import com.marshall.pyerite.characterMasteryModule.data.CharacterMasteryLoader
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryGroupViewModel
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryOverviewViewModel
import com.marshall.pyerite.characterMasteryModule.viewModel.CharacterMasteryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterMasteryModule = module {
    singleOf(::CharacterMasteryLoader)
    singleOf(::CharacterMasteryRepository)
    viewModelOf(::CharacterMasteryOverviewViewModel)
    viewModelOf(::CharacterMasteryGroupViewModel)
}
