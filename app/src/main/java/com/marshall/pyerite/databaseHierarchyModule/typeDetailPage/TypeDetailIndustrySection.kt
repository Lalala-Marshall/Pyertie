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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.databaseHierarchyModule.room.entity.MetaGroupEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeApplicableBlueprintCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeBlueprintDetail
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputItem
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningOutputSummary
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceCount
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeRefiningSourceItem
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale

private val IndustryIconSize = 24.dp
private val IndustryIconGap = 8.dp
/** Leading indent for expanded submenu rows. */
private val IndustrySubMenuIndent = 16.dp
private val IndustryTitleValueGap = 24.dp
private val IndustryRowHorizontalPadding = 12.dp
private val IndustryRowVerticalPadding = 12.dp
private val IndustryChevronSize = 20.dp

@Composable
fun TypeDetailIndustrySection(
    typeId: Int,
    applicableBlueprintCount: TypeApplicableBlueprintCount?,
    refiningSourceCount: TypeRefiningSourceCount?,
    blueprints: List<TypeBlueprintDetail>,
    refiningOutputSummary: TypeRefiningOutputSummary?,
    refiningOutputs: List<TypeRefiningOutputItem>,
    refiningSources: List<TypeRefiningSourceItem>,
    metaGroups: List<MetaGroupEntity>,
    navController: NavController,
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

    var refiningOutputExpanded by remember { mutableStateOf(false) }
    var refiningSourceExpanded by remember { mutableStateOf(false) }

    val applicableValue = applicableBlueprintCount?.count?.let { count ->
        stringResource(R.string.type_detail_industry_type_count, formatIndustryCount(count))
    }
    val refiningSourceValue = refiningSourceCount?.count?.let { count ->
        stringResource(R.string.type_detail_industry_type_count, formatIndustryCount(count))
    }
    val metaGroupNameById = remember(metaGroups) { metaGroups.associate { it.id to it.name } }

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
                    showDivider = hasContentBelowApplicable && !refiningSourceExpanded,
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeApplicableBlueprints.create(typeId))
                    },
                )
            }

            if (showRefiningSource && refiningSourceValue != null) {
                TypeDetailIndustryExpandableHeaderRow(
                    iconFileName = null,
                    label = stringResource(R.string.type_detail_refining_source),
                    value = refiningSourceValue,
                    expanded = refiningSourceExpanded,
                    showDivider = !refiningSourceExpanded && hasContentBelowRefiningSource,
                    onClick = { refiningSourceExpanded = !refiningSourceExpanded },
                )
                if (refiningSourceExpanded) {
                    refiningSources.forEachIndexed { index, source ->
                        val hasContentBelow = index != refiningSources.lastIndex ||
                            blueprints.isNotEmpty() || showRefiningOutput
                        TypeDetailIndustryMaterialRow(
                            iconFileName = source.iconFilename?.takeIf {
                                iconManager.getIconFile(it) != null
                            },
                            label = formatRefiningSourceLabel(
                                name = source.name,
                                metaGroupId = source.metaGroupId,
                                metaGroupNameById = metaGroupNameById,
                            ),
                            value = source.quantityPerUnit?.let { qty ->
                                stringResource(
                                    R.string.type_detail_refining_per_unit,
                                    formatIndustryCount(qty),
                                )
                            }.orEmpty(),
                            showDivider = hasContentBelow,
                            onClick = {
                                navController.navigate(DatabaseRoute.TypeDetail.create(source.typeId))
                            },
                        )
                    }
                }
            }

            blueprints.forEachIndexed { index, blueprint ->
                val hasContentBelowBlueprint = index != blueprints.lastIndex ||
                    (showRefiningOutput && !refiningOutputExpanded)
                val blueprintIconFileName = blueprint.iconFilename?.takeIf { fileName ->
                    iconManager.getIconFile(fileName) != null
                }
                TypeDetailIndustryMaterialRow(
                    iconFileName = blueprintIconFileName,
                    label = blueprint.name ?: stringResource(R.string.type_detail_blueprint),
                    value = "",
                    subMenuIndent = false,
                    showDivider = hasContentBelowBlueprint,
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(blueprint.typeId))
                    },
                )
            }

            if (showRefiningOutput && refiningOutputSummary != null) {
                val processSize = refiningOutputSummary.processSize ?: 1
                TypeDetailIndustryExpandableHeaderRow(
                    iconFileName = null,
                    label = stringResource(R.string.type_detail_refining_output),
                    labelSubtitle = stringResource(
                        R.string.type_detail_refining_output_per_unit,
                        processSize,
                    ),
                    value = stringResource(
                        R.string.type_detail_refining_output_count,
                        refiningOutputSummary.outputMaterialCount,
                    ),
                    expanded = refiningOutputExpanded,
                    showDivider = false,
                    onClick = { refiningOutputExpanded = !refiningOutputExpanded },
                )
                if (refiningOutputExpanded) {
                    refiningOutputs.forEachIndexed { index, output ->
                        TypeDetailIndustryMaterialRow(
                            iconFileName = output.iconFilename?.takeIf {
                                iconManager.getIconFile(it) != null
                            },
                            label = output.name.orEmpty(),
                            value = output.quantity?.let { qty ->
                                stringResource(
                                    R.string.type_detail_refining_quantity,
                                    formatIndustryCount(qty),
                                )
                            }.orEmpty(),
                            showDivider = index != refiningOutputs.lastIndex,
                            onClick = {
                                navController.navigate(DatabaseRoute.TypeDetail.create(output.typeId))
                            },
                        )
                    }
                }
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
    onClick: () -> Unit,
    iconManager: IconManager = koinInject(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        IndustryPrimaryRowContent(
            iconFileName = iconFileName,
            label = label,
            labelSubtitle = labelSubtitle,
            value = value,
            trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            trailingIconTint = colorResource(R.color.hint_text),
            iconManager = iconManager,
        )

        if (showDivider) ItemDivider()
    }
}

@Composable
private fun TypeDetailIndustryExpandableHeaderRow(
    iconFileName: String?,
    label: String,
    labelSubtitle: String? = null,
    value: String,
    expanded: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager = koinInject(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        IndustryPrimaryRowContent(
            iconFileName = iconFileName,
            label = label,
            labelSubtitle = labelSubtitle,
            value = value,
            trailingIcon = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            trailingIconTint = colorResource(if (expanded) R.color.text_primary else R.color.hint_text),
            iconManager = iconManager,
        )

        if (showDivider) ItemDivider()
    }
}

@Composable
private fun TypeDetailIndustryMaterialRow(
    iconFileName: String?,
    label: String,
    value: String,
    subMenuIndent: Boolean = true,
    showDivider: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager = koinInject(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        IndustryMaterialRowContent(
            iconFileName = iconFileName,
            label = label,
            value = value,
            subMenuIndent = subMenuIndent,
            iconManager = iconManager,
        )

        if (showDivider) ItemDivider()
    }
}

@Composable
private fun IndustryPrimaryRowContent(
    iconFileName: String?,
    label: String,
    labelSubtitle: String?,
    value: String,
    trailingIcon: ImageVector,
    trailingIconTint: Color,
    iconManager: IconManager,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IndustryRowHorizontalPadding,
                vertical = IndustryRowVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IndustryRowLeadingIcon(iconFileName = iconFileName, iconManager = iconManager)

        if (labelSubtitle != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = label,
                    color = colorResource(R.color.text_primary),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
                Text(
                    text = labelSubtitle,
                    color = colorResource(R.color.text_caption),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        } else {
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
                lineHeight = 20.sp,
            )
        }

        Spacer(modifier = Modifier.width(IndustryTitleValueGap))

        Text(
            text = value,
            color = colorResource(R.color.hint_text),
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            modifier = Modifier.size(IndustryChevronSize),
            tint = trailingIconTint,
        )
    }
}

@Composable
private fun IndustryMaterialRowContent(
    iconFileName: String?,
    label: String,
    value: String,
    subMenuIndent: Boolean,
    iconManager: IconManager,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = IndustryRowHorizontalPadding,
                vertical = IndustryRowVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (subMenuIndent) {
            Spacer(modifier = Modifier.width(IndustrySubMenuIndent))
        }

        IndustryRowLeadingIcon(iconFileName = iconFileName, iconManager = iconManager)

        Text(
            modifier = Modifier.weight(1f),
            text = label,
            color = colorResource(R.color.text_primary),
            fontSize = 16.sp,
            lineHeight = 20.sp,
        )

        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(IndustryTitleValueGap))

            Text(
                text = value,
                color = colorResource(R.color.hint_text),
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(IndustryChevronSize),
            tint = colorResource(R.color.hint_text),
        )
    }
}

@Composable
private fun IndustryRowLeadingIcon(
    iconFileName: String?,
    iconManager: IconManager,
) {
    val iconFile = iconFileName?.let { iconManager.getIconFile(it) }
    if (iconFile != null) {
        Icon(
            modifier = Modifier.size(IndustryIconSize),
            painter = rememberAsyncImagePainter(iconFile),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(IndustryIconGap))
    }
}

private fun formatRefiningSourceLabel(
    name: String?,
    metaGroupId: Int?,
    metaGroupNameById: Map<Int, String?>,
): String {
    val baseName = name.orEmpty()
    if (metaGroupId == null) return baseName
    val metaLabel = metaGroupNameById[metaGroupId]?.takeIf { it.isNotBlank() }
        ?: "$metaGroupId"
    return "$baseName ($metaLabel)"
}

private fun formatIndustryCount(count: Int): String =
    NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
