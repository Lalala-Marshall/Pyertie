package com.marshall.pyerite.data.sde

import com.marshall.pyerite.data.sde.network.SdeRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sdeModule = module {
    single { SdeVersionStore(androidContext()) }
    single { SdeRemoteDataSource.create() }
    single {
        SdeUpdateRepository(
            context = androidContext(),
            remoteDataSource = get(),
            versionStore = get(),
            roomProvider = get(),
            iconManager = get(),
        )
    }
    single {
        SdeUpdateController(
            repository = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
    }
    viewModel { SdeUpdateViewModel(get()) }
}
