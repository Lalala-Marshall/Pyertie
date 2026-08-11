package com.marshall.pyerite.characterSkillsModule.model

import com.marshall.pyerite.localization.ContentLanguage

/** Results for skill-plan clipboard import / export from the detail ViewModel. */
internal sealed interface SkillPlanExportResult {
    data class Success(val text: String) : SkillPlanExportResult
    data object EmptyPlan : SkillPlanExportResult
}

internal sealed interface SkillPlanImportResult {
    data object Success : SkillPlanImportResult
    data object ClipboardEmpty : SkillPlanImportResult
    data object ParseFailed : SkillPlanImportResult
}

internal enum class SkillPlanExportLanguage {
    CHINESE,
    ENGLISH,
    ;

    fun toContentLanguage(): ContentLanguage = when (this) {
        CHINESE -> ContentLanguage.CHINESE
        ENGLISH -> ContentLanguage.ENGLISH
    }
}
