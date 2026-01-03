package com.marshall.pyerite.application

import android.app.Application
import com.marshall.pyerite.data.appModule
import com.marshall.pyerite.data.db.DatabaseInitializer
import com.marshall.pyerite.databaseHierarchyModule.viewModel.databaseHierarchyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PyeriteApp : Application() {

    override fun onCreate() {
        super.onCreate()

        DatabaseInitializer.init(this)  // 保留原始 DB 拷贝
        startKoin {
            androidContext(this@PyeriteApp)
            modules(
                appModule,
                databaseHierarchyModule
            )
        }
    }
}
