package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Immutable
data class PyeriteTopBarActionItem(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val contentDescription: String,
    val label: String? = null,
    val accentColor: Color = Color.Unspecified,
    val iconTint: Color = Color.Unspecified,
    val enabled: Boolean = true,
    val iconBadge: Boolean = false,
    val showIcon: Boolean = true,
)

/**
 * Top bar action cluster: circle for a lone icon-only action; pill when any label
 * is present or multiple actions are grouped on the same side.
 */
@Composable
fun PyeriteTopBarActions(
    actions: List<PyeriteTopBarActionItem>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return

    val usePill = actions.size > 1 || actions.any { !it.label.isNullOrBlank() }
    val actionHeight = dimensionResource(R.dimen.top_bar_back_button_size)

    if (!usePill) {
        TopBarActionSegment(
            action = actions.single(),
            inPill = false,
            modifier = modifier
                .size(actionHeight)
                .topBarActionSurface(CircleShape),
        )
        return
    }

    val pillShape = RoundedCornerShape(percent = 50)
    val horizontalPadding = dimensionResource(R.dimen.top_bar_action_pill_horizontal_padding)

    Row(
        modifier = modifier
            .height(actionHeight)
            .topBarActionSurface(pillShape)
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.top_bar_action_pill_item_gap)),
    ) {
        actions.forEach { action ->
            TopBarActionSegment(
                action = action,
                inPill = true,
            )
        }
    }
}

@Composable
private fun TopBarActionSegment(
    action: PyeriteTopBarActionItem,
    inPill: Boolean,
    modifier: Modifier = Modifier,
) {
    val buttonSize = dimensionResource(R.dimen.top_bar_back_button_size)
    val iconSize = dimensionResource(R.dimen.top_bar_icon_size)
    val badgeSize = dimensionResource(R.dimen.top_bar_update_badge_size)
    val badgeIconSize = dimensionResource(R.dimen.top_bar_update_badge_icon_size)
    val textStartPadding = dimensionResource(R.dimen.top_bar_update_text_start_padding)
    val textEndPadding = dimensionResource(R.dimen.top_bar_update_text_end_padding)
    val textEndGap = dimensionResource(R.dimen.top_bar_update_text_end_gap)
    val defaultIconTint = colorResource(R.color.text_primary)
    val accentColor = if (action.accentColor != Color.Unspecified) {
        action.accentColor
    } else {
        defaultIconTint
    }
    val iconTint = if (action.iconTint != Color.Unspecified) {
        action.iconTint
    } else {
        defaultIconTint
    }
    val hasLabel = !action.label.isNullOrBlank()

    val content: @Composable () -> Unit = {
        if (hasLabel) {
            Text(
                text = requireNotNull(action.label),
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.width(textEndGap))
        }

        if (action.iconBadge) {
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.contentDescription,
                    tint = colorResource(R.color.white),
                    modifier = Modifier.size(badgeIconSize),
                )
            }
        } else if (action.showIcon) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }

    if (hasLabel) {
        Row(
            modifier = modifier
                .clickable(
                    enabled = action.enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = action.onClick,
                )
                .semantics { role = Role.Button }
                .padding(
                    start = if (inPill) textStartPadding else 0.dp,
                    end = if (inPill) textEndPadding else 0.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
        return
    }

    Box(
        modifier = modifier
            .then(if (inPill) Modifier.size(buttonSize) else Modifier)
            .clickable(
                enabled = action.enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = action.onClick,
            )
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun Modifier.topBarActionSurface(shape: Shape): Modifier {
    return this
        .shadow(elevation = 2.dp, shape = shape, clip = false)
        .clip(shape)
        .background(colorResource(R.color.second_background))
}
