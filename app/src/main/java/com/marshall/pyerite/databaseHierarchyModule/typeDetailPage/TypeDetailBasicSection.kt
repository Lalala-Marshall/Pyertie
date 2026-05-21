package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeTraitDetail
import com.marshall.pyerite.databaseHierarchyModule.util.formatTraitBonusAnnotated
import com.marshall.pyerite.databaseHierarchyModule.util.formatTypeDescriptionAnnotated
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import org.koin.compose.koinInject

private data class TraitBlocks(
    val roleTraits: List<TypeTraitDetail>,
    val skillTraitGroups: List<Pair<Int, List<TypeTraitDetail>>>,
)

private fun buildTraitBlocks(traits: List<TypeTraitDetail>): TraitBlocks {
    if (traits.isEmpty()) return TraitBlocks(emptyList(), emptyList())
    val sorted = traits.sortedWith(
        compareBy<TypeTraitDetail> { it.importance ?: Int.MAX_VALUE }
            .thenBy { it.skill }
    )
    val roleTraits = sorted.filter { it.skill == -1 }
    val skillIds = sorted.filter { it.skill > 0 }.map { it.skill }.distinct()
    val skillTraitGroups = skillIds
        .sortedBy { sid ->
            sorted.filter { it.skill == sid }
                .minOfOrNull { it.importance ?: Int.MAX_VALUE } ?: Int.MAX_VALUE
        }
        .map { sid -> sid to sorted.filter { it.skill == sid } }
    return TraitBlocks(roleTraits, skillTraitGroups)
}

private fun TypeTraitDetail.skillDisplayName(): String =
    skillZhName?.takeIf { it.isNotBlank() }
        ?: skillEnName?.takeIf { it.isNotBlank() }
        ?: ""

@Composable
fun TypeSummarySection(
    entity: TypeEntity,
    traits: List<TypeTraitDetail>,
    iconManager: IconManager = koinInject()
) {
    val linkColor = colorResource(R.color.hyperlink_text)
    val traitBlocks = remember(traits) { buildTraitBlocks(traits) }
    val hasTraitUi = traitBlocks.roleTraits.isNotEmpty() || traitBlocks.skillTraitGroups.isNotEmpty()

    BaseContainer(useSystemBarsPadding = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(64.dp),
                    painter = entity.iconFilename?.let {
                        rememberAsyncImagePainter(iconManager.getIconFile(it))
                    } ?: painterResource(R.drawable.ic_database),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = entity.zhName ?: entity.name.orEmpty(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary)
                    )
                    Text(
                        text = "${entity.categoryName ?: ""} / ${entity.groupName ?: ""} / ID:${entity.id}",
                        fontSize = 12.sp,
                        color = colorResource(R.color.hint_text)
                    )
                }
            }

            val desc = entity.description
            if (!desc.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = colorResource(R.color.border)
                )
                Text(
                    text = formatTypeDescriptionAnnotated(desc),
                    fontSize = 14.sp,
                    color = colorResource(R.color.text_primary),
                    lineHeight = 20.sp
                )
            }

            if (hasTraitUi) {
                Spacer(modifier = Modifier.height(if (!desc.isNullOrBlank()) 20.dp else 12.dp))
                if (traitBlocks.roleTraits.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.type_detail_role_bonus_header),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.text_primary),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    traitBlocks.roleTraits.forEach { trait ->
                        TraitBulletLine(trait.content, linkColor)
                    }
                }
                traitBlocks.skillTraitGroups.forEachIndexed { index, (skillTypeId, list) ->
                    if (traitBlocks.roleTraits.isNotEmpty() || index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    val skillLabel = list.firstOrNull()?.skillDisplayName().orEmpty()
                        .ifBlank {
                            stringResource(
                                R.string.type_detail_skill_bonus_unknown_name,
                                skillTypeId
                            )
                        }
                    Text(
                        text = buildAnnotatedString {
                            append("- ")
                            pushStyle(
                                SpanStyle(
                                    color = linkColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            append(skillLabel)
                            pop()
                            append(stringResource(R.string.type_detail_skill_bonus_header_suffix))
                        },
                        fontSize = 14.sp,
                        color = colorResource(R.color.text_primary),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    list.forEach { trait ->
                        TraitBulletLine(trait.content, linkColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TraitBulletLine(content: String, linkColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u00B7 ",
            fontSize = 14.sp,
            color = colorResource(R.color.text_primary),
            lineHeight = 20.sp
        )
        Text(
            text = formatTraitBonusAnnotated(content, linkColor),
            fontSize = 14.sp,
            color = colorResource(R.color.text_primary),
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}