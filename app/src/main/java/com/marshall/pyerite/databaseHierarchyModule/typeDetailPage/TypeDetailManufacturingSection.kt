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
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingMaterial
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingProduct
import com.marshall.pyerite.sdeModule.room.industry.BlueprintManufacturingSkill
import com.marshall.pyerite.iconModule.manager.IconManager
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
import org.koin.compose.koinInject

private val ManufacturingExpandAnimation = expandVertically(expandFrom = Alignment.Top)
private val ManufacturingCollapseAnimation = shrinkVertically(shrinkTowards = Alignment.Top)

@Composable
fun TypeDetailManufacturingSection(
    typeId: Int,
    products: List<BlueprintManufacturingProduct>,
    materials: List<BlueprintManufacturingMaterial>,
    skills: List<BlueprintManufacturingSkill>,
    manufacturingTimeSeconds: Int?,
    navController: NavController,
    iconManager: IconManager = koinInject(),
) {
    val formattedTime = formatDurationFromSeconds(manufacturingTimeSeconds)
    if (products.isEmpty() && materials.isEmpty() && skills.isEmpty() && formattedTime.isEmpty()) {
        return
    }

    var materialsExpanded by rememberSaveable(typeId) { mutableStateOf(false) }
    var skillsExpanded by rememberSaveable(typeId) { mutableStateOf(false) }

    val product = products.firstOrNull()
    val showProduct = product != null
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
        title = stringResource(R.string.type_detail_section_manufacturing),
        useSystemBarsPadding = false,
    ) {
        Column {
            if (showProduct) {
                val productName = product.name.orEmpty()
                val quantity = product.quantity ?: 1
                val hasContentBelowProduct = showMaterials || showSkills || showTime
                val productIconFileName = product.iconFilename?.takeIf {
                    iconManager.getIconFile(it) != null
                }
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconFileName = productIconFileName,
                        showLeadingIcon = productIconFileName != null,
                        itemName = stringResource(R.string.type_detail_manufacturing_product),
                        trailingValue = stringResource(
                            R.string.type_detail_manufacturing_product_quantity_name,
                            quantity,
                            productName,
                        ),
                        showChevron = true,
                        onClick = {
                            navController.navigate(DatabaseRoute.TypeDetail.create(product.typeId))
                        },
                    ),
                    showDivider = hasContentBelowProduct && !materialsExpanded,
                    iconManager = iconManager,
                )
            }

            if (showMaterials) {
                val hasContentBelowMaterials = showSkills || showTime
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        showLeadingIcon = false,
                        itemName = stringResource(R.string.type_detail_manufacturing_materials),
                        trailingValue = materialsCountLabel,
                        showChevron = true,
                        chevronExpanded = materialsExpanded,
                        onClick = { materialsExpanded = !materialsExpanded },
                    ),
                    showDivider = !materialsExpanded && hasContentBelowMaterials,
                )
                AnimatedVisibility(
                    visible = materialsExpanded,
                    enter = ManufacturingExpandAnimation,
                    exit = ManufacturingCollapseAnimation,
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
                        itemName = stringResource(R.string.type_detail_manufacturing_skills),
                        trailingValue = skillsCountLabel,
                        showChevron = true,
                        chevronExpanded = skillsExpanded,
                        onClick = { skillsExpanded = !skillsExpanded },
                    ),
                    showDivider = !skillsExpanded && showTime,
                )
                AnimatedVisibility(
                    visible = skillsExpanded,
                    enter = ManufacturingExpandAnimation,
                    exit = ManufacturingCollapseAnimation,
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
                        itemName = stringResource(R.string.type_detail_manufacturing_time),
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
