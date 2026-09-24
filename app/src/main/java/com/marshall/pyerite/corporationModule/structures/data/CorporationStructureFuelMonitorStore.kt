package com.marshall.pyerite.corporationModule.structures.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureFuelMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-wide fuel-monitor window. Default is [CorporationStructureFuelMonitor.NONE]. */
internal class CorporationStructureFuelMonitorStore(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val monitorFlow = MutableStateFlow(read())

    fun monitor(): StateFlow<CorporationStructureFuelMonitor> = monitorFlow.asStateFlow()

    fun set(monitor: CorporationStructureFuelMonitor) {
        prefs.edit(commit = true) {
            putInt(KEY_FUEL_MONITOR_DAYS, monitor.days)
        }
        monitorFlow.value = monitor
    }

    private fun read(): CorporationStructureFuelMonitor =
        CorporationStructureFuelMonitor.fromDays(
            prefs.getInt(KEY_FUEL_MONITOR_DAYS, CorporationStructureFuelMonitor.NONE.days),
        )

    private companion object {
        const val PREFS_NAME = "pyerite_corporation_structures"
        const val KEY_FUEL_MONITOR_DAYS = "fuel_monitor_days"
    }
}
