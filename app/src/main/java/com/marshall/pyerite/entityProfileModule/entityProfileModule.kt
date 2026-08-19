package com.marshall.pyerite.entityProfileModule

import com.marshall.pyerite.entityProfileModule.data.EntityProfileLoader
import com.marshall.pyerite.entityProfileModule.viewModel.EntityProfileViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val entityProfileModule = module {
    singleOf(::EntityProfileLoader)
    viewModelOf(::EntityProfileViewModel)
}
