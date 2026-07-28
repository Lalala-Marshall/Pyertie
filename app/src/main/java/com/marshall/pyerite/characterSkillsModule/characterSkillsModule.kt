package com.marshall.pyerite.characterSkillsModule

import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsCache
import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsLoader
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsRepository
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterSkillsModule = module {
    single { CharacterSkillsCache(androidContext()) }
    singleOf(::CharacterSkillsLoader)
    singleOf(::CharacterSkillsRepository)
    viewModelOf(::CharacterSkillsViewModel)
}
