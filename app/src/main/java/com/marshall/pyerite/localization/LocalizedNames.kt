package com.marshall.pyerite.localization

import com.marshall.pyerite.sdeModule.room.dogma.TypeTraitDetail

fun localizedName(    zh: String?,
    en: String?,
    fallback: String?,
    language: ContentLanguage,
): String {
    val value = when (language) {
        ContentLanguage.CHINESE -> zh ?: fallback ?: en
        ContentLanguage.ENGLISH -> en ?: fallback ?: zh
    }
    return value.orEmpty()
}

fun LocalizableName.displayName(controller: LocaleController): String =
    localizedName(zhName, enName, name, controller.contentLanguage)

/** Room @Query projection; cannot implement [LocalizableName] (no [LocalizableName.name] column). */
fun TypeTraitDetail.displayName(controller: LocaleController): String =
    localizedName(zhName, enName, null, controller.contentLanguage)