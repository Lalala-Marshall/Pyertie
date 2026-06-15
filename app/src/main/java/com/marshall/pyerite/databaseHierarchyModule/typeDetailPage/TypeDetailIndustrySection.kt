package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import org.koin.compose.koinInject

private val IndustryExpandAnimation = expandVertically(expandFrom = Alignment.Top)
private val IndustryCollapseAnimation = shrinkVertically(shrinkTowards = Alignment.Top)

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

    var refiningSourceExpanded by rememberSaveable(typeId) { mutableStateOf(false) }
    var refiningOutputExpanded by rememberSaveable(typeId) { mutableStateOf(false) }

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
                TypeDetailIndustryNavRow(
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
                AnimatedVisibility(
                    visible = refiningSourceExpanded,
                    enter = IndustryExpandAnimation,
                    exit = IndustryCollapseAnimation,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        refiningSources.forEachIndexed { index, source ->
                            val hasContentBelow = index != refiningSources.lastIndex ||
                                blueprints.isNotEmpty() || showRefiningOutput
                            BaseSubMenuRow(
                                model = BaseSubMenuRowModel(
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
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(source.typeId),
                                        )
                                    },
                                ),
                                showDivider = hasContentBelow,
                            )
                        }
                    }
                }
            }

            blueprints.forEachIndexed { index, blueprint ->
                val hasContentBelowBlueprint = index != blueprints.lastIndex ||
                    (showRefiningOutput && !refiningOutputExpanded)
                val blueprintIconFileName = blueprint.iconFilename?.takeIf { fileName ->
                    iconManager.getIconFile(fileName) != null
                }
                BaseSubMenuRow(
                    model = BaseSubMenuRowModel(
                        iconFileName = blueprintIconFileName,
                        label = blueprint.name ?: stringResource(R.string.type_detail_blueprint),
                        subMenuIndent = false,
                        onClick = {
                            navController.navigate(DatabaseRoute.TypeDetail.create(blueprint.typeId))
                        },
                    ),
                    showDivider = hasContentBelowBlueprint,
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
                AnimatedVisibility(
                    visible = refiningOutputExpanded,
                    enter = IndustryExpandAnimation,
                    exit = IndustryCollapseAnimation,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        refiningOutputs.forEachIndexed { index, output ->
                            BaseSubMenuRow(
                                model = BaseSubMenuRowModel(
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
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(output.typeId),
                                        )
                                    },
                                ),
                                showDivider = index != refiningOutputs.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeDetailIndustryNavRow(
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
private fun IndustryPrimaryRowContent(
    iconFileName: String?,
    label: String,
    labelSubtitle: String?,
    value: String,
    trailingIcon: ImageVector,
    trailingIconTint: Color,
    iconManager: IconManager,
) {
    val iconSize = dimensionResource(R.dimen.detail_row_icon_size)
    val iconGap = dimensionResource(R.dimen.detail_row_icon_gap)
    val titleValueGap = dimensionResource(R.dimen.detail_row_title_value_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val trailingGap = dimensionResource(R.dimen.detail_row_trailing_gap)
    val labelSubtitleSpacing = dimensionResource(R.dimen.detail_row_label_subtitle_spacing)
    val labelTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val labelLineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    val subtitleTextSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val subtitleLineHeight = dimensionResource(R.dimen.detail_row_label_subtitle_line_height).value.sp
    val valueTextSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IndustryRowLeadingIcon(
            iconSize = iconSize,
            iconGap = iconGap,
            iconFileName = iconFileName,
            iconManager = iconManager,
        )

        if (labelSubtitle != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(labelSubtitleSpacing),
            ) {
                Text(
                    text = label,
                    color = colorResource(R.color.text_primary),
                    fontSize = labelTextSize,
                    lineHeight = labelLineHeight,
                )
                Text(
                    text = labelSubtitle,
                    color = colorResource(R.color.text_caption),
                    fontSize = subtitleTextSize,
                    lineHeight = subtitleLineHeight,
                )
            }
        } else {
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                color = colorResource(R.color.text_primary),
                fontSize = labelTextSize,
                lineHeight = labelLineHeight,
            )
        }

        Spacer(modifier = Modifier.width(titleValueGap))

        Text(
            text = value,
            color = colorResource(R.color.hint_text),
            fontSize = valueTextSize,
        )

        Spacer(modifier = Modifier.width(trailingGap))

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            modifier = Modifier.size(chevronSize),
            tint = trailingIconTint,
        )
    }
}

@Composable
private fun IndustryRowLeadingIcon(
    iconSize: Dp,
    iconGap: Dp,
    iconFileName: String?,
    iconManager: IconManager,
) {
    val iconFile = iconFileName?.let { iconManager.getIconFile(it) }
    if (iconFile != null) {
        Icon(
            modifier = Modifier.size(iconSize),
            painter = rememberAsyncImagePainter(iconFile),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.width(iconGap))
    }
}
