package com.marshall.pyerite.characterClonesModule

import com.marshall.pyerite.characterClonesModule.data.CharacterClonesCache
import com.marshall.pyerite.characterClonesModule.data.CharacterClonesLoader
import com.marshall.pyerite.characterClonesModule.viewModel.CharacterClonesRepository
import com.marshall.pyerite.characterClonesModule.viewModel.CharacterClonesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterClonesModule = module {
    single { CharacterClonesCache(androidContext()) }
    singleOf(::CharacterClonesLoader)
    singleOf(::CharacterClonesRepository)
    viewModelOf(::CharacterClonesViewModel)
}
