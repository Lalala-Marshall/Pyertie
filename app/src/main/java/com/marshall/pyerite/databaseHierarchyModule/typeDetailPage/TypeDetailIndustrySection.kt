package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.sdeModule.room.catalog.MetaGroupEntity
import com.marshall.pyerite.sdeModule.room.industry.TypeApplicableBlueprintCount
import com.marshall.pyerite.sdeModule.room.industry.TypeBlueprintDetail
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningOutputItem
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningOutputSummary
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningSourceCount
import com.marshall.pyerite.sdeModule.room.industry.TypeRefiningSourceItem
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
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
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_applicable_to),
                        itemHint = stringResource(R.string.type_detail_applicable_to_subtitle),
                        trailingValue = applicableValue,
                        showChevron = true,
                        onClick = {
                            navController.navigate(DatabaseRoute.TypeApplicableBlueprints.create(typeId))
                        },
                    ),
                    showDivider = hasContentBelowApplicable && !refiningSourceExpanded,
                )
            }

            if (showRefiningSource && refiningSourceValue != null) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_refining_source),
                        trailingValue = refiningSourceValue,
                        showChevron = true,
                        chevronExpanded = refiningSourceExpanded,
                        onClick = { refiningSourceExpanded = !refiningSourceExpanded },
                    ),
                    showDivider = !refiningSourceExpanded && hasContentBelowRefiningSource,
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
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_refining_output),
                        itemHint = stringResource(
                            R.string.type_detail_refining_output_per_unit,
                            processSize,
                        ),
                        trailingValue = stringResource(
                            R.string.type_detail_refining_output_count,
                            refiningOutputSummary.outputMaterialCount,
                        ),
                        showChevron = true,
                        chevronExpanded = refiningOutputExpanded,
                        onClick = { refiningOutputExpanded = !refiningOutputExpanded },
                    ),
                    showDivider = false,
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
