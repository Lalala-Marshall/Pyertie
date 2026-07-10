package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.marshall.pyerite.R
import androidx.compose.ui.unit.dp

@Composable
fun PyeriteBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonSize = dimensionResource(R.dimen.top_bar_back_button_size)
    val iconSize = dimensionResource(R.dimen.top_bar_icon_size)

    Box(
        modifier = modifier
            .size(buttonSize)
            .shadow(elevation = 2.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(colorResource(R.color.second_background))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.nav_back),
            tint = colorResource(R.color.text_primary),
            modifier = Modifier.size(iconSize),
        )
    }
}
