package com.marshall.pyerite.loyaltyPointsModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.loyaltyPointsModule.model.LoyaltyPointsConfig
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
internal fun LoyaltyPointsLoadFailedBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.character_sheet_load_failed),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.character_sheet_retry))
        }
    }
}

@Composable
internal fun LoyaltyPointsPlaceholderRow(text: String) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = text,
            showChevron = false,
            onClick = null,
        ),
        showDivider = false,
    )
}

@Composable
internal fun LoyaltyMilitiaTag() {
    val tagTextSize = dimensionResource(R.dimen.loyalty_points_militia_tag_text_size).value.sp
    Text(
        text = stringResource(R.string.loyalty_points_militia_tag),
        color = colorResource(R.color.loyalty_points_militia_tag_text),
        fontSize = tagTextSize,
        lineHeight = tagTextSize,
        fontWeight = FontWeight.SemiBold,
        style = TextStyle(
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.loyalty_points_militia_tag_corner)))
            .background(colorResource(R.color.loyalty_points_militia_tag))
            .padding(
                horizontal = dimensionResource(R.dimen.loyalty_points_militia_tag_horizontal_padding),
                vertical = dimensionResource(R.dimen.loyalty_points_militia_tag_vertical_padding),
            ),
    )
}

@Composable
internal fun LoyaltyPointsSectionItemRow(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
    indexInSection: Int,
    sectionItemCount: Int,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = loyaltyPointsSectionItemShape(indexInSection, sectionItemCount, cardCornerRadius)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        BaseLazyColumnItem(model = model, showDivider = showDivider)
    }
}

internal fun loyaltyPointsSectionItemShape(
    indexInSection: Int,
    sectionItemCount: Int,
    corner: Dp,
): Shape {
    return when {
        sectionItemCount == 1 -> RoundedCornerShape(corner)
        indexInSection == 0 -> RoundedCornerShape(topStart = corner, topEnd = corner)
        indexInSection == sectionItemCount - 1 ->
            RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
        else -> RectangleShape
    }
}

@Composable
internal fun loyaltyPointsSystemSecurityColor(security: Double): Color = when {
    security <= 0.0 -> colorResource(R.color.character_security_negative)
    security < LoyaltyPointsConfig.SECURITY_LOW_THRESHOLD ->
        colorResource(R.color.character_security_low)
    else -> colorResource(R.color.character_security_high)
}
