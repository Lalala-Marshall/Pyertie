package com.marshall.pyerite.characterModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.marshall.pyerite.R

@Composable
fun CharacterAvatar(
    portraitUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = dimensionResource(R.dimen.character_main_avatar_size),
    corporationIconUrl: String? = null,
    allianceIconUrl: String? = null,
    showBadges: Boolean = false,
) {
    val badgeSize = dimensionResource(R.dimen.character_badge_size)
    val badgeOffset = dimensionResource(R.dimen.character_badge_offset)

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(
                    width = dimensionResource(R.dimen.character_avatar_border_width),
                    color = colorResource(R.color.border),
                    shape = CircleShape,
                )
                .background(colorResource(R.color.main_background), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (portraitUrl.isNullOrBlank()) {
                Icon(
                    painter = painterResource(R.drawable.ic_character_avatar_placeholder),
                    contentDescription = stringResource(R.string.character_portrait),
                    modifier = Modifier.size(size * 0.55f),
                    tint = colorResource(R.color.hint_text),
                )
            } else {
                AsyncImage(
                    model = portraitUrl,
                    contentDescription = stringResource(R.string.character_portrait),
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (showBadges) {
            CharacterBadgeIcon(
                iconUrl = corporationIconUrl,
                contentDescription = stringResource(R.string.character_corp_icon),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = -badgeOffset, y = badgeOffset)
                    .size(badgeSize),
            )
            CharacterBadgeIcon(
                iconUrl = allianceIconUrl,
                contentDescription = stringResource(R.string.character_alliance_icon),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = badgeOffset, y = badgeOffset)
                    .size(badgeSize),
            )
        }
    }
}

@Composable
private fun CharacterBadgeIcon(
    iconUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = dimensionResource(R.dimen.character_avatar_border_width),
                color = colorResource(R.color.second_background),
                shape = CircleShape,
            )
            .background(colorResource(R.color.main_background), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (iconUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_character_avatar_placeholder),
                contentDescription = contentDescription,
                modifier = Modifier.size(dimensionResource(R.dimen.character_org_icon_size)),
                tint = colorResource(R.color.hint_text),
            )
        } else {
            AsyncImage(
                model = iconUrl,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
