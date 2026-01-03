package com.marshall.pyerite.data

import com.marshall.pyerite.data.db.RoomProvider
import com.marshall.pyerite.data.icons.IconManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { RoomProvider(androidContext()) }
    single { IconManager(androidContext()) }
}