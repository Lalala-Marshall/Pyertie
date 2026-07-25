package com.marshall.pyerite.infra.network

import org.koin.dsl.module

val networkModule = module {
    single { createOkHttpClient() }
}
