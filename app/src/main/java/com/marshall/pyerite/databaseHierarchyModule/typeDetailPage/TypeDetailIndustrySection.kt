package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowSubtitleModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailSubtitleRow

@Composable
fun TypeDetailIndustrySection(
    blueprints: List<TypeBlueprintDetail>,
    refiningOutputSummary: TypeRefiningOutputSummary?,
) {
    val showRefiningOutput = (refiningOutputSummary?.outputMaterialCount ?: 0) > 0
    if (!hasIndustrySectionContent(blueprints, refiningOutputSummary)) return

    BaseContainer(
        title = stringResource(R.string.industrial_info),
        useSystemBarsPadding = false,
    ) {
        Column {
            blueprints.forEachIndexed { index, blueprint ->
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = blueprint.iconFilename,
                        label = blueprint.name ?: stringResource(R.string.type_detail_blueprint),
                        value = "",
                    ),
                    showDivider = index != blueprints.lastIndex || showRefiningOutput,
                )
            }

            if (showRefiningOutput && refiningOutputSummary != null) {
                val processSize = refiningOutputSummary.processSize ?: 1
                BaseDetailSubtitleRow(
                    model = BaseDetailRowSubtitleModel(
                        iconRes = R.drawable.ic_database,
                        label = stringResource(R.string.type_detail_refining_output),
                        labelSubtitle = stringResource(
                            R.string.type_detail_refining_output_per_unit,
                            processSize,
                        ),
                        value = stringResource(
                            R.string.type_detail_refining_output_count,
                            refiningOutputSummary.outputMaterialCount,
                        ),
                    ),
                    showDivider = false,
                )
            }
        }
    }
}
