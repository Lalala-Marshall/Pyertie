package com.marshall.pyerite.iconModule

import com.marshall.pyerite.iconModule.manager.IconManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val iconModule = module {
    single { IconManager(androidContext()) }
}
