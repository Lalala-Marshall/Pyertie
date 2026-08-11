package com.marshall.pyerite.characterSkillsModule

import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsCache
import com.marshall.pyerite.characterSkillsModule.data.CharacterSkillsLoader
import com.marshall.pyerite.characterSkillsModule.data.SkillPlanItemCatalogLoader
import com.marshall.pyerite.characterSkillsModule.data.SkillPlanStore
import com.marshall.pyerite.characterSkillsModule.data.SkillPrerequisiteResolver
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsRepository
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanDetailViewModel
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanRepository
import com.marshall.pyerite.characterSkillsModule.viewModel.SkillPlanViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val characterSkillsModule = module {
    single { CharacterSkillsCache(androidContext()) }
    single { SkillPlanStore(androidContext()) }
    singleOf(::CharacterSkillsLoader)
    singleOf(::SkillPrerequisiteResolver)
    singleOf(::SkillPlanItemCatalogLoader)
    singleOf(::CharacterSkillsRepository)
    singleOf(::SkillPlanRepository)
    viewModelOf(::CharacterSkillsViewModel)
    viewModelOf(::SkillPlanViewModel)
    viewModelOf(::SkillPlanDetailViewModel)
}
