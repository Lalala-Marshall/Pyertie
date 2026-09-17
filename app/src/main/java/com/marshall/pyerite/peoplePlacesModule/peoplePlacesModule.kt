package com.marshall.pyerite.peoplePlacesModule

import com.marshall.pyerite.peoplePlacesModule.data.PeoplePlacesLoader
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesRepository
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val peoplePlacesModule = module {
    singleOf(::PeoplePlacesLoader)
    singleOf(::PeoplePlacesRepository)
    viewModelOf(::PeoplePlacesViewModel)
}
