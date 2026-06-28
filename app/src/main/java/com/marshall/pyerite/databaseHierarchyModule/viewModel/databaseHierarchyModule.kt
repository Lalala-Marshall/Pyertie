package com.marshall.pyerite.databaseHierarchyModule.viewModel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseHierarchyModule = module {
    single { DatabaseRepository(get(), get()) }
    viewModel { DatabaseViewModel(get(), get(), get()) }
}