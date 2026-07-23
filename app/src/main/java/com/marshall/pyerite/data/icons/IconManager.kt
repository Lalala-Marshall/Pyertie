package com.marshall.pyerite.data.icons

import android.content.Context
import com.marshall.pyerite.data.sde.SdeAssetFiles
import java.io.File

class IconManager(private val context: Context) {

    private val iconIndex = mutableMapOf<String, File>()

    init {
        initIcons()
    }

    fun reload() {
        initIcons()
    }

    private fun initIcons() {
        SdeAssetFiles.ensureIconsPresent(context)
        buildIndex(File(context.filesDir, SdeAssetFiles.ICONS_DIR_NAME))
    }

    private fun buildIndex(iconDir: File) {
        iconIndex.clear()
        if (!iconDir.exists()) return
        iconDir.walkTopDown().forEach { file ->
            if (file.isFile) iconIndex[file.name] = file
        }
    }

    fun getIconFile(iconFileName: String?): File? {
        if (iconFileName.isNullOrBlank()) return null
        return iconIndex[iconFileName]
    }
}
