package com.marshall.pyerite.eveAuthModule

import com.marshall.pyerite.data.network.NetworkDefaults
import com.marshall.pyerite.data.network.createApi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val eveAuthModule = module {
    single {
        get<OkHttpClient>().createApi<EveSsoApi>(NetworkDefaults.PLACEHOLDER_BASE_URL)
    }
    single { EveSsoCallbackBus() }
    single { EveTokenStore(androidContext()) }
    single { EvePendingLoginStore(androidContext()) }
    singleOf(::EveSsoRemoteDataSource)
    singleOf(::EveTokenManager)
}
