package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.integerResource
import com.marshall.pyerite.R

@Composable
fun TypeDetailMaterialResearchSection(
    typeId: Int,
    researchMaterialTimeSeconds: Int?,
) {
    TypeDetailBlueprintResearchSection(
        typeId = typeId,
        baseTimeSeconds = researchMaterialTimeSeconds,
        sectionTitleRes = R.string.type_detail_section_material_research,
        researchTimeLabelRes = R.string.type_detail_material_research_time,
    )
}

@Composable
fun TypeDetailTimeResearchSection(
    typeId: Int,
    researchTimeTimeSeconds: Int?,
) {
    val teLevelStep = integerResource(R.integer.blueprint_time_efficiency_research_level_step)
    TypeDetailBlueprintResearchSection(
        typeId = typeId,
        baseTimeSeconds = researchTimeTimeSeconds,
        sectionTitleRes = R.string.type_detail_section_time_research,
        researchTimeLabelRes = R.string.type_detail_time_research_time,
        researchLevelStep = teLevelStep,
    )
}
