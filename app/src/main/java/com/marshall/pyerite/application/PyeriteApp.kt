package com.marshall.pyerite.application

import android.app.Application
import com.marshall.pyerite.infra.appModule
import com.marshall.pyerite.infra.network.networkModule
import com.marshall.pyerite.sdeModule.update.BundledSdeUpdater
import com.marshall.pyerite.sdeModule.update.SdeUpdateController
import com.marshall.pyerite.sdeModule.update.SdeUpdateLog
import com.marshall.pyerite.sdeModule.sdeModule
import com.marshall.pyerite.characterSheetModule.characterSheetModule
import com.marshall.pyerite.charactersListModule.charactersListModule
import com.marshall.pyerite.databaseHierarchyModule.databaseHierarchyModule
import com.marshall.pyerite.esiModule.esiModule
import com.marshall.pyerite.eveAuthModule.eveAuthModule
import com.marshall.pyerite.iconModule.iconModule
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
                iconModule,
                sdeModule,
                eveAuthModule,
                esiModule,
                databaseHierarchyModule,
                charactersListModule,
                characterSheetModule,
            )
        }

        applicationScope.launch {
            SdeUpdateLog.d("PyeriteApp", "starting background update check")
            GlobalContext.get().get<SdeUpdateController>().checkForUpdates()
        }
    }
}
