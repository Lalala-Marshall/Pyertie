package com.marshall.pyerite.data.sde

import com.marshall.pyerite.data.network.createApi
import com.marshall.pyerite.data.network.createPyeriteOkHttpClient
import com.marshall.pyerite.data.sde.network.SdeRemoteApi
import com.marshall.pyerite.data.sde.network.SdeRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

val sdeModule = module {
    single { SdeVersionStore(androidContext()) }
    single {
        val base = get<OkHttpClient>()
        val metadataClient = createPyeriteOkHttpClient(
            connectTimeoutSeconds = SdeRemoteDataSource.METADATA_CONNECT_TIMEOUT_SECONDS,
            readTimeout = SdeRemoteDataSource.METADATA_READ_TIMEOUT_SECONDS,
            base = base,
        )
        val downloadClient = createPyeriteOkHttpClient(
            connectTimeoutSeconds = SdeRemoteDataSource.DOWNLOAD_CONNECT_TIMEOUT_SECONDS,
            readTimeout = SdeRemoteDataSource.DOWNLOAD_READ_TIMEOUT_MINUTES,
            readTimeoutUnit = TimeUnit.MINUTES,
            base = base,
        )
        SdeRemoteDataSource(
            metadataApi = metadataClient.createApi<SdeRemoteApi>(SdeRemoteConfig.RETROFIT_BASE_URL),
            downloadApi = downloadClient.createApi<SdeRemoteApi>(SdeRemoteConfig.RETROFIT_BASE_URL),
        )
    }
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
