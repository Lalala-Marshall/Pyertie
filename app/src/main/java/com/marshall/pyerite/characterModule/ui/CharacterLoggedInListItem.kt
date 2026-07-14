package com.marshall.pyerite.characterModule.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterModule.model.LoggedInCharacter
import com.marshall.pyerite.characterModule.model.SkillQueueProgress
import com.marshall.pyerite.ui.golbalComponents.ItemDivider
import java.util.Locale

@Composable
fun CharacterLoggedInListItem(
    modifier: Modifier = Modifier,
    character: LoggedInCharacter,
    showDivider: Boolean,
    isEditMode: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val innerPadding = dimensionResource(R.dimen.character_card_inner_padding)
    val clickAction = when {
        isEditMode && onDeleteClick != null -> onDeleteClick
        !isEditMode && onClick != null -> onClick
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (clickAction != null) {
                    Modifier
                        .semantics { role = Role.Button }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = clickAction,
                        )
                } else {
                    Modifier
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterAvatar(
                portraitUrl = character.portraitUrl,
                size = dimensionResource(R.dimen.character_list_avatar_size),
                corporationIconUrl = character.corporationIconUrl,
                allianceIconUrl = character.allianceIconUrl,
                showBadges = true,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.detail_row_icon_gap)))
            Column(modifier = Modifier.weight(1f)) {
                val nameSize = dimensionResource(R.dimen.character_list_name_text_size).value.sp
                val metaSize = dimensionResource(R.dimen.character_list_meta_text_size).value.sp
                Text(
                    text = character.name,
                    color = colorResource(R.color.text_primary),
                    fontWeight = FontWeight.Bold,
                    fontSize = nameSize,
                    lineHeight = nameSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = compactTextStyle(nameSize),
                )
                CharacterLocationLine(
                    securityStatus = character.securityStatus,
                    location = character.location,
                    locationStatus = character.locationStatus,
                    textSize = metaSize,
                )
                character.walletBalance?.let { balance ->
                    Text(
                        text = stringResource(R.string.character_wallet, balance),
                        color = colorResource(R.color.text_caption),
                        fontSize = metaSize,
                        lineHeight = metaSize,
                        style = compactTextStyle(metaSize),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
                    )
                }
                if (character.totalSkillPoints != null) {
                    Text(
                        text = stringResource(
                            R.string.character_total_skill_points,
                            character.totalSkillPoints,
                            character.unallocatedSkillPoints.orEmpty(),
                        ),
                        color = colorResource(R.color.text_caption),
                        fontSize = metaSize,
                        lineHeight = metaSize,
                        style = compactTextStyle(metaSize),
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
                    )
                }
                character.skillQueue?.let { queue ->
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.character_skill_queue_top_spacing)))
                    CharacterSkillQueueSection(queue = queue, textSize = metaSize)
                }
            }
            if (isEditMode) {
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.character_delete_icon_gap)))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.character_delete),
                    tint = colorResource(R.color.character_delete),
                    modifier = Modifier.size(dimensionResource(R.dimen.character_delete_icon_size)),
                )
            }
        }
        if (showDivider) {
            ItemDivider()
        }
    }
}

@Composable
private fun CharacterLocationLine(
    securityStatus: Double?,
    location: String?,
    locationStatus: String?,
    textSize: TextUnit,
) {
    if (securityStatus == null && location.isNullOrBlank() && locationStatus.isNullOrBlank()) return

    val securityColor = when {
        securityStatus == null -> colorResource(R.color.text_primary)
        securityStatus < 0.0 -> colorResource(R.color.character_security_negative)
        securityStatus < 0.5 -> colorResource(R.color.character_security_low)
        else -> colorResource(R.color.character_security_high)
    }

    Text(
        text = buildAnnotatedString {
            securityStatus?.let { status ->
                withStyle(SpanStyle(color = securityColor)) {
                    append(String.format(Locale.US, "%.1f", status))
                }
                append("  ")
            }
            location?.let { loc ->
                withStyle(SpanStyle(color = colorResource(R.color.text_primary))) {
                    append(loc)
                }
            }
            locationStatus?.let { status ->
                append(" ")
                withStyle(SpanStyle(color = colorResource(R.color.text_caption))) {
                    append(status)
                }
            }
        },
        fontSize = textSize,
        lineHeight = textSize,
        style = compactTextStyle(textSize),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = dimensionResource(R.dimen.character_list_meta_spacing)),
    )
}

@Composable
private fun CharacterSkillQueueSection(
    queue: SkillQueueProgress,
    textSize: TextUnit,
) {
    val trackColor = colorResource(R.color.character_skill_progress_track)
    val activeColor = colorResource(R.color.character_skill_progress_active)
    val progressShape = RoundedCornerShape(dimensionResource(R.dimen.character_skill_progress_corner_radius))

    LinearProgressIndicator(
        progress = { queue.progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.character_skill_progress_height))
            .clip(progressShape),
        color = activeColor,
        trackColor = trackColor,
        drawStopIndicator = {},
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = dimensionResource(R.dimen.character_skill_queue_label_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = activeColor,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.character_skill_queue_icon_gap)),
        )
        Text(
            text = stringResource(R.string.character_training_skill, queue.skillName, queue.level),
            modifier = Modifier.weight(1f),
            color = colorResource(R.color.text_primary),
            fontSize = textSize,
            lineHeight = textSize,
            style = compactTextStyle(textSize),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = queue.timeRemaining,
            color = colorResource(R.color.text_caption),
            fontSize = textSize,
            lineHeight = textSize,
            style = compactTextStyle(textSize),
        )
    }
}

@Composable
private fun compactTextStyle(lineHeight: TextUnit): TextStyle = TextStyle(
    lineHeight = lineHeight,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)
