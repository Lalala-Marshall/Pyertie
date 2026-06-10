package com.marshall.pyerite.localization

object SdeDatabase {
    const val ZH_FILE_NAME = "item_db_zh.sqlite"
    const val EN_FILE_NAME = "item_db_en.sqlite"

    fun fileName(language: ContentLanguage): String = when (language) {
        ContentLanguage.CHINESE -> ZH_FILE_NAME
        ContentLanguage.ENGLISH -> EN_FILE_NAME
    }
}
