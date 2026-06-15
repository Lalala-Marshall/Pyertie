package com.marshall.pyerite.data

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.localization.LocaleController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { LocaleController(androidContext()) }
    single { RoomProvider(androidContext(), get()) }
    single { IconManager(androidContext()) }
}