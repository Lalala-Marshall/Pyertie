package com.marshall.pyerite.localization

import java.util.Locale

/** Resolved in-app content language for SDE data and bilingual name fields. */
enum class ContentLanguage {
    CHINESE,
    ENGLISH,
    ;

    companion object {
        fun fromLocale(locale: Locale): ContentLanguage =
            if (locale.language.lowercase(Locale.ROOT).startsWith("zh")) CHINESE else ENGLISH
    }
}
