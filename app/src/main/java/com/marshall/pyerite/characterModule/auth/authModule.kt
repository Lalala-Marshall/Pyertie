package com.marshall.pyerite.characterModule.auth

import com.marshall.pyerite.data.network.NetworkDefaults
import com.marshall.pyerite.data.network.createApi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authModule = module {
    single {
        get<OkHttpClient>().createApi<EveSsoApi>(NetworkDefaults.PLACEHOLDER_BASE_URL)
    }
    single {
        get<OkHttpClient>().createApi<EsiApi>(EveSsoConfig.ESI_BASE_URL)
    }
    single { EveSsoCallbackBus() }
    single { EveTokenStore(androidContext()) }
    singleOf(::EveSsoRemoteDataSource)
    singleOf(::EsiPublicDataSource)
    single {
        EveSsoAuthRepository(
            remote = get(),
            tokenStore = get(),
            esi = get(),
            characterRepository = get(),
            callbackBus = get(),
        )
    }
}
