package com.marshall.pyerite.eveAuthModule

import com.marshall.pyerite.eveAuthModule.sso.EvePendingLoginStore
import com.marshall.pyerite.eveAuthModule.sso.EveSsoApi
import com.marshall.pyerite.eveAuthModule.sso.EveSsoCallbackBus
import com.marshall.pyerite.eveAuthModule.sso.EveSsoRemoteDataSource
import com.marshall.pyerite.eveAuthModule.token.EveTokenManager
import com.marshall.pyerite.eveAuthModule.token.EveTokenStore
import com.marshall.pyerite.infra.network.NetworkDefaults
import com.marshall.pyerite.infra.network.createApi
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
