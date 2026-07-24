package com.marshall.pyerite.infra

import com.marshall.pyerite.localization.LocaleController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** App-wide non-domain singletons (locale, etc.). */
val appModule = module {
    single { LocaleController(androidContext()) }
}
