package com.marshall.pyerite.characterSheetModule.viewModel

import com.marshall.pyerite.characterSheetModule.data.CharacterSheetLoader
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterSheetModule = module {
    singleOf(::CharacterSheetLoader)
    single {
        CharacterSheetRepository(
            sheetLoader = get(),
            characterRepository = get(),
        )
    }
    viewModelOf(::CharacterSheetViewModel)
}