package com.marshall.pyerite.personalPropertyModule

import com.marshall.pyerite.personalPropertyModule.data.PersonalPropertyLoader
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyRepository
import com.marshall.pyerite.personalPropertyModule.viewModel.PersonalPropertyViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val personalPropertyModule = module {
    singleOf(::PersonalPropertyLoader)
    singleOf(::PersonalPropertyRepository)
    viewModelOf(::PersonalPropertyViewModel)
}
