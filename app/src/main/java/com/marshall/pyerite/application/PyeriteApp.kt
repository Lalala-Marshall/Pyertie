package com.marshall.pyerite.application

import android.app.Application
import com.marshall.pyerite.data.appModule
import com.marshall.pyerite.data.sde.BundledSdeUpdater
import com.marshall.pyerite.data.sde.sdeModule
import com.marshall.pyerite.databaseHierarchyModule.viewModel.databaseHierarchyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PyeriteApp : Application() {

    override fun onCreate() {
        super.onCreate()

        BundledSdeUpdater.ensureUpToDate(this)
        startKoin {
            androidContext(this@PyeriteApp)
            modules(
                appModule,
                sdeModule,
                databaseHierarchyModule
            )
        }
    }
}
