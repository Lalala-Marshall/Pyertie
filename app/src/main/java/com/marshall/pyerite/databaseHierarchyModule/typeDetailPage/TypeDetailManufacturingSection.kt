package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingMaterial
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingProduct
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintManufacturingSkill
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
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
    val formattedTime = remember(manufacturingTimeSeconds) {
        formatDurationFromSeconds(manufacturingTimeSeconds)
    }
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
                ManufacturingNavRow(
                    iconFileName = product.iconFilename?.takeIf {
                        iconManager.getIconFile(it) != null
                    },
                    label = stringResource(R.string.type_detail_manufacturing_product),
                    value = stringResource(
                        R.string.type_detail_manufacturing_product_quantity_name,
                        quantity,
                        productName,
                    ),
                    showDivider = hasContentBelowProduct && !materialsExpanded,
                    onClick = {
                        navController.navigate(DatabaseRoute.TypeDetail.create(product.typeId))
                    },
                    iconManager = iconManager,
                )
            }

            if (showMaterials) {
                val hasContentBelowMaterials = showSkills || showTime
                ManufacturingExpandableHeaderRow(
                    label = stringResource(R.string.type_detail_manufacturing_materials),
                    value = materialsCountLabel,
                    expanded = materialsExpanded,
                    showDivider = !materialsExpanded && hasContentBelowMaterials,
                    onClick = { materialsExpanded = !materialsExpanded },
                    iconManager = iconManager,
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
                ManufacturingExpandableHeaderRow(
                    label = stringResource(R.string.type_detail_manufacturing_skills),
                    value = skillsCountLabel,
                    expanded = skillsExpanded,
                    showDivider = !skillsExpanded && showTime,
                    onClick = { skillsExpanded = !skillsExpanded },
                    iconManager = iconManager,
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
                ManufacturingValueRow(
                    label = stringResource(R.string.type_detail_manufacturing_time),
                    value = formattedTime,
                    iconManager = iconManager,
                )
            }
        }
    }
}

@Composable
private fun ManufacturingNavRow(
    iconFileName: String?,
    label: String,
    value: String,
    showDivider: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        ManufacturingPrimaryRowContent(
            iconFileName = iconFileName,
            label = label,
            value = value,
            trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            trailingIconTint = colorResource(R.color.hint_text),
            iconManager = iconManager,
        )
        if (showDivider) ItemDivider()
    }
}

@Composable
private fun ManufacturingValueRow(
    label: String,
    value: String,
    iconManager: IconManager,
) {
    ManufacturingPrimaryRowContent(
        iconFileName = null,
        label = label,
        value = value,
        trailingIcon = null,
        trailingIconTint = Color.Unspecified,
        iconManager = iconManager,
    )
}

@Composable
private fun ManufacturingExpandableHeaderRow(
    label: String,
    value: String,
    expanded: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    iconManager: IconManager,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        ManufacturingPrimaryRowContent(
            iconFileName = null,
            label = label,
            value = value,
            trailingIcon = if (expanded) {
                Icons.Filled.KeyboardArrowDown
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            trailingIconTint = colorResource(
                if (expanded) R.color.text_primary else R.color.hint_text,
            ),
            iconManager = iconManager,
        )
        if (showDivider) ItemDivider()
    }
}

@Composable
private fun ManufacturingPrimaryRowContent(
    iconFileName: String?,
    label: String,
    value: String,
    trailingIcon: ImageVector?,
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
    val labelTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val labelLineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    val valueTextSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ManufacturingRowLeadingIcon(
            iconSize = iconSize,
            iconGap = iconGap,
            iconFileName = iconFileName,
            iconManager = iconManager,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = label,
            color = colorResource(R.color.text_primary),
            fontSize = labelTextSize,
            lineHeight = labelLineHeight,
        )

        if (value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(titleValueGap))
            Text(
                text = value,
                color = colorResource(R.color.hint_text),
                fontSize = valueTextSize,
            )
        }

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(trailingGap))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(chevronSize),
                tint = trailingIconTint,
            )
        }
    }
}

@Composable
private fun ManufacturingRowLeadingIcon(
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
