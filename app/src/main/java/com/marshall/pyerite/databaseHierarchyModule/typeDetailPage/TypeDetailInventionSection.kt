package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionMaterial
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionProduct
import com.marshall.pyerite.sdeModule.room.industry.BlueprintInventionSkill
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
import org.koin.compose.koinInject

private val InventionExpandAnimation = expandVertically(expandFrom = Alignment.Top)
private val InventionCollapseAnimation = shrinkVertically(shrinkTowards = Alignment.Top)

@Composable
fun TypeDetailInventionSection(
    typeId: Int,
    products: List<BlueprintInventionProduct>,
    materials: List<BlueprintInventionMaterial>,
    skills: List<BlueprintInventionSkill>,
    inventionTimeSeconds: Int?,
    navController: NavController,
    iconManager: IconManager = koinInject(),
) {
    val formattedTime = formatDurationFromSeconds(inventionTimeSeconds)
    if (products.isEmpty() && materials.isEmpty() && skills.isEmpty() && formattedTime.isEmpty()) {
        return
    }

    var materialsExpanded by rememberSaveable(typeId) { mutableStateOf(false) }
    var skillsExpanded by rememberSaveable(typeId) { mutableStateOf(false) }

    val showProducts = products.isNotEmpty()
    val showMaterials = materials.isNotEmpty()
    val showSkills = skills.isNotEmpty()
    val showTime = formattedTime.isNotEmpty()

    val materialsCountLabel = stringResource(
        R.string.type_detail_industry_type_count,
        formatIndustryCount(materials.size),
    )
    val skillsCountLabel = stringResource(
        R.string.type_detail_industry_type_count,
        formatIndustryCount(skills.size),
    )

    BaseContainer(
        title = stringResource(R.string.type_detail_section_invention),
        useSystemBarsPadding = false,
    ) {
        Column {
            if (showProducts) {
                products.forEachIndexed { index, product ->
                    val successRate = formatInventionProbability(product.probability)
                    val hasContentBelowProduct = index != products.lastIndex ||
                        showMaterials ||
                        showSkills ||
                        showTime
                    val successRateHint = successRate.takeIf { it.isNotEmpty() }?.let { rate ->
                        stringResource(R.string.type_detail_invention_success_rate, rate)
                    }.orEmpty()
                    BaseSubMenuRow(
                        model = BaseSubMenuRowModel(
                            subMenuIndent = false,
                            iconFileName = product.iconFilename?.takeIf {
                                iconManager.getIconFile(it) != null
                            },
                            label = product.name.orEmpty(),
                            labelHint = successRateHint,
                            onClick = {
                                navController.navigate(DatabaseRoute.TypeDetail.create(product.typeId))
                            },
                        ),
                        showDivider = hasContentBelowProduct &&
                            !(index == products.lastIndex && materialsExpanded),
                    )
                }
            }

            if (showMaterials) {
                val hasContentBelowMaterials = showSkills || showTime
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_invention_materials),
                        trailingValue = materialsCountLabel,
                        showChevron = true,
                        chevronExpanded = materialsExpanded,
                        onClick = { materialsExpanded = !materialsExpanded },
                    ),
                    showDivider = !materialsExpanded && hasContentBelowMaterials,
                )
                AnimatedVisibility(
                    visible = materialsExpanded,
                    enter = InventionExpandAnimation,
                    exit = InventionCollapseAnimation,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        materials.forEachIndexed { index, material ->
                            val quantityLabel = material.quantity?.let { qty ->
                                stringResource(R.string.type_detail_refining_quantity, formatIndustryCount(qty))
                            }.orEmpty()
                            BaseSubMenuRow(
                                model = BaseSubMenuRowModel(
                                    iconFileName = material.iconFilename?.takeIf {
                                        iconManager.getIconFile(it) != null
                                    },
                                    label = material.name.orEmpty(),
                                    value = quantityLabel,
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(material.typeId),
                                        )
                                    },
                                ),
                                showDivider = index != materials.lastIndex ||
                                    (showSkills && !skillsExpanded) ||
                                    showTime,
                            )
                        }
                    }
                }
            }

            if (showSkills) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_invention_skills),
                        trailingValue = skillsCountLabel,
                        showChevron = true,
                        chevronExpanded = skillsExpanded,
                        onClick = { skillsExpanded = !skillsExpanded },
                    ),
                    showDivider = !skillsExpanded && showTime,
                )
                AnimatedVisibility(
                    visible = skillsExpanded,
                    enter = InventionExpandAnimation,
                    exit = InventionCollapseAnimation,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        skills.forEachIndexed { index, skill ->
                            val level = skill.level ?: 0
                            BaseSubMenuRow(
                                model = BaseSubMenuRowModel(
                                    iconFileName = skill.iconFilename?.takeIf {
                                        iconManager.getIconFile(it) != null
                                    },
                                    label = skill.name.orEmpty(),
                                    value = stringResource(R.string.skill_level, level),
                                    onClick = {
                                        navController.navigate(
                                            DatabaseRoute.TypeDetail.create(skill.typeId),
                                        )
                                    },
                                ),
                                showDivider = index != skills.lastIndex || showTime,
                            )
                        }
                    }
                }
            }

            if (showTime) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_invention_time),
                        trailingValue = formattedTime,
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = false,
                )
            }
        }
    }
}
