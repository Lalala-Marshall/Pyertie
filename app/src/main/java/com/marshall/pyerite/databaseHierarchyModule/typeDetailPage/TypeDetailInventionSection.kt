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
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionMaterial
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionProduct
import com.marshall.pyerite.databaseHierarchyModule.room.entity.BlueprintInventionSkill
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuRowModel
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
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
                InventionExpandableHeaderRow(
                    label = stringResource(R.string.type_detail_invention_materials),
                    value = materialsCountLabel,
                    expanded = materialsExpanded,
                    showDivider = !materialsExpanded && hasContentBelowMaterials,
                    onClick = { materialsExpanded = !materialsExpanded },
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
                InventionExpandableHeaderRow(
                    label = stringResource(R.string.type_detail_invention_skills),
                    value = skillsCountLabel,
                    expanded = skillsExpanded,
                    showDivider = !skillsExpanded && showTime,
                    onClick = { skillsExpanded = !skillsExpanded },
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
                InventionValueRow(
                    label = stringResource(R.string.type_detail_invention_time),
                    value = formattedTime,
                )
            }
        }
    }
}

@Composable
private fun InventionValueRow(
    label: String,
    value: String,
) {
    InventionPrimaryRowContent(
        iconFileName = null,
        label = label,
        value = value,
        trailingIcon = null,
        trailingIconTint = Color.Unspecified,
        iconManager = null,
    )
}

@Composable
private fun InventionExpandableHeaderRow(
    label: String,
    value: String,
    expanded: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        InventionPrimaryRowContent(
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
            iconManager = null,
        )
        if (showDivider) ItemDivider()
    }
}

@Composable
private fun InventionPrimaryRowContent(
    iconFileName: String?,
    label: String,
    value: String,
    trailingIcon: ImageVector?,
    trailingIconTint: Color,
    iconManager: IconManager?,
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
        if (iconManager != null) {
            InventionRowLeadingIcon(
                iconSize = iconSize,
                iconGap = iconGap,
                iconFileName = iconFileName,
                iconManager = iconManager,
            )
        }

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
private fun InventionRowLeadingIcon(
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
