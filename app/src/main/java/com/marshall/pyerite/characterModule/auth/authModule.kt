package com.marshall.pyerite.characterModule.auth

import androidx.lifecycle.ProcessLifecycleOwner
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
    single { EvePendingLoginStore(androidContext()) }
    single { CharacterSelectionStore(androidContext()) }
    singleOf(::EveSsoRemoteDataSource)
    singleOf(::EveTokenManager)
    singleOf(::EsiPublicDataSource)
    singleOf(::CharacterProfileLoader)
    single {
        EveSsoAuthRepository(
            remote = get(),
            tokenManager = get(),
            profileLoader = get(),
            characterRepository = get(),
            callbackBus = get(),
            pendingLoginStore = get(),
        )
    }
    single(createdAtStart = true) {
        EveTokenLifecycleObserver(get()).also { observer ->
            ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        }
    }
}
