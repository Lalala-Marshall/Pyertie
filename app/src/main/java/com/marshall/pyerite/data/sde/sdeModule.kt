package com.marshall.pyerite.data.sde

import com.marshall.pyerite.data.sde.network.SdeRemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sdeModule = module {
    single { SdeVersionStore(androidContext()) }
    single { SdeRemoteDataSource.createApi() }
    single { SdeRemoteDataSource(get()) }
    single {
        SdeUpdateRepository(
            context = androidContext(),
            remoteDataSource = get(),
            versionStore = get(),
            roomProvider = get(),
            iconManager = get(),
        )
    }
    viewModel { SdeUpdateViewModel(get()) }
}
