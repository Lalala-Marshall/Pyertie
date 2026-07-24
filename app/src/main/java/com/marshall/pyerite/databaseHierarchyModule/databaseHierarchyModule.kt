package com.marshall.pyerite.databaseHierarchyModule

import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseRepository
import com.marshall.pyerite.databaseHierarchyModule.viewModel.DatabaseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseHierarchyModule = module {
    single { DatabaseRepository(get(), get()) }
    viewModel { DatabaseViewModel(get(), get(), get()) }
}
