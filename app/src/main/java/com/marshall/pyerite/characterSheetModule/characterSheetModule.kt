package com.marshall.pyerite.characterSheetModule

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.marshall.pyerite.characterSheetModule.data.CharacterSheetLoader
import com.marshall.pyerite.characterSheetModule.viewModel.CharacterSheetRepository
import com.marshall.pyerite.characterSheetModule.viewModel.CharacterSheetViewModel

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
