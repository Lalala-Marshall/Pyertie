package com.marshall.pyerite.application

import android.app.Application
import com.marshall.pyerite.data.appModule
import com.marshall.pyerite.characterModule.auth.authModule
import com.marshall.pyerite.data.network.networkModule
import com.marshall.pyerite.data.sde.BundledSdeUpdater
import com.marshall.pyerite.data.sde.SdeUpdateController
import com.marshall.pyerite.data.sde.SdeUpdateLog
import com.marshall.pyerite.data.sde.sdeModule
import com.marshall.pyerite.characterModule.viewModel.characterModule
import com.marshall.pyerite.databaseHierarchyModule.viewModel.databaseHierarchyModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class PyeriteApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // SDE must exist before auth restore (createdAtStart) resolves skill/location names.
        runBlocking(Dispatchers.IO) {
            BundledSdeUpdater.ensureUpToDate(this@PyeriteApp)
        }

        startKoin {
            androidContext(this@PyeriteApp)
            modules(
                appModule,
                networkModule,
                sdeModule,
                authModule,
                databaseHierarchyModule,
                characterModule,
            )
        }

        applicationScope.launch {
            SdeUpdateLog.d("PyeriteApp", "starting background update check")
            GlobalContext.get().get<SdeUpdateController>().checkForUpdates()
        }
    }
}
