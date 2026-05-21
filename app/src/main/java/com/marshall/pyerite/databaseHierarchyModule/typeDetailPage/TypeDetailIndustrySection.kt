package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowSubtitleModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailSubtitleRow
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TypeDetailIndustrySection(
    applicableBlueprintCount: TypeApplicableBlueprintCount?,
    refiningSourceCount: TypeRefiningSourceCount?,
    blueprints: List<TypeBlueprintDetail>,
    refiningOutputSummary: TypeRefiningOutputSummary?,
    iconManager: IconManager = koinInject(),
) {
    val showApplicable = shouldShowIndustryCount(applicableBlueprintCount?.count)
    val showRefiningSource = shouldShowIndustryCount(refiningSourceCount?.count)
    val showRefiningOutput = (refiningOutputSummary?.outputMaterialCount ?: 0) > 0
    if (!hasIndustrySectionContent(
            blueprints,
            refiningOutputSummary,
            applicableBlueprintCount,
            refiningSourceCount,
        )
    ) {
        return
    }

    val applicableValue = applicableBlueprintCount?.count?.let { count ->
        stringResource(R.string.type_detail_industry_type_count, formatIndustryCount(count))
    }
    val refiningSourceValue = refiningSourceCount?.count?.let { count ->
        stringResource(R.string.type_detail_industry_type_count, formatIndustryCount(count))
    }
    val hasContentBelowApplicable = showRefiningSource || blueprints.isNotEmpty() || showRefiningOutput
    val hasContentBelowRefiningSource = blueprints.isNotEmpty() || showRefiningOutput

    BaseContainer(
        title = stringResource(R.string.industrial_info),
        useSystemBarsPadding = false,
    ) {
        Column {
            if (showApplicable && applicableValue != null) {
                TypeDetailIndustrySummaryRow(
                    iconFileName = null,
                    label = stringResource(R.string.type_detail_applicable_to),
                    labelSubtitle = stringResource(R.string.type_detail_applicable_to_subtitle),
                    value = applicableValue,
                    showDivider = hasContentBelowApplicable,
                )
            }

            if (showRefiningSource && refiningSourceValue != null) {
                TypeDetailIndustrySummaryRow(
                    iconFileName = null,
                    label = stringResource(R.string.type_detail_refining_source),
                    value = refiningSourceValue,
                    showDivider = hasContentBelowRefiningSource,
                )
            }

            blueprints.forEachIndexed { index, blueprint ->
                val hasContentBelowBlueprint = index != blueprints.lastIndex || showRefiningOutput
                val blueprintIconFileName = blueprint.iconFilename?.takeIf { fileName ->
                    iconManager.getIconFile(fileName) != null
                }
                BaseDetailRow(
                    model = BaseDetailRowModel(
                        iconFileName = blueprintIconFileName,
                        label = blueprint.name ?: stringResource(R.string.type_detail_blueprint),
                        value = "",
                    ),
                    showDivider = hasContentBelowBlueprint,
                )
            }

            if (showRefiningOutput && refiningOutputSummary != null) {
                val processSize = refiningOutputSummary.processSize ?: 1
                BaseDetailSubtitleRow(
                    model = BaseDetailRowSubtitleModel(
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

@Composable
private fun TypeDetailIndustrySummaryRow(
    iconFileName: String?,
    label: String,
    labelSubtitle: String? = null,
    value: String,
    showDivider: Boolean,
    onClick: () -> Unit = {},
    iconManager: IconManager = koinInject(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconFile = iconFileName?.let { iconManager.getIconFile(it) }
            if (iconFile != null) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = rememberAsyncImagePainter(iconFile),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            if (labelSubtitle != null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Text(
                        text = label,
                        color = colorResource(R.color.text_primary),
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                    )
                    Text(
                        text = labelSubtitle,
                        color = colorResource(R.color.text_caption),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    )
                }
            } else {
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    color = colorResource(R.color.text_primary),
                    fontSize = 16.sp,
                )
            }

            Text(
                text = value,
                color = colorResource(R.color.hint_text),
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorResource(R.color.hint_text),
            )
        }

        if (showDivider) ItemDivider()
    }
}

private fun formatIndustryCount(count: Int): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
