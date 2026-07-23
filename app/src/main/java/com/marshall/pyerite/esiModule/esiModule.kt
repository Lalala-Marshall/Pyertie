package com.marshall.pyerite.esiModule

import com.marshall.pyerite.data.network.createApi
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val esiModule = module {
    single {
        get<OkHttpClient>().createApi<EsiApi>(EsiConfig.BASE_URL)
    }
    singleOf(::EsiPublicDataSource)
}
