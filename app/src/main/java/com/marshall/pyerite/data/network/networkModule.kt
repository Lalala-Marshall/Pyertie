package com.marshall.pyerite.data.network

import org.koin.dsl.module

val networkModule = module {
    single { createPyeriteOkHttpClient() }
}
