package com.marshall.pyerite.data.icons

import android.content.Context
import java.io.File
import java.util.zip.ZipInputStream

class IconManager(private val context: Context) {

    companion object {
        private const val ICONS_ZIP_PATH = "icons/icons.zip"
        private const val ICONS_DIR_NAME = "icons"
    }

    private val iconIndex = mutableMapOf<String, File>()

    init {
        initIcons()
    }

    private fun initIcons() {
        val iconDir = File(context.filesDir, ICONS_DIR_NAME)
        if (!iconDir.exists() || iconDir.list()?.isEmpty() != false) {
            iconDir.mkdirs()
            context.assets.open(ICONS_ZIP_PATH).use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val outFile = File(iconDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { output ->
                            zip.copyTo(output)
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        }
        buildIndex(iconDir)
    }

    private fun buildIndex(iconDir: File) {
        iconIndex.clear()
        iconDir.walkTopDown().forEach { file ->
            if (file.isFile) iconIndex[file.name] = file
        }
    }

    fun getIconFile(iconFileName: String?) = iconIndex[iconFileName]
}
