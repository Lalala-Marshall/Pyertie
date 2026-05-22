package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.ItemDivider

/**
 * Section 2 — market: layout only; API wiring comes later.
 */
@Composable
fun TypeDetailMarketSection(
    onOpenRegionMarket: () -> Unit = {},
) {
    val cardHorizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val marketCardVerticalPadding = dimensionResource(R.dimen.type_detail_market_card_vertical_padding)
    val dividerThickness = dimensionResource(R.dimen.detail_divider_thickness)
    val marketDividerHeight = dimensionResource(R.dimen.type_detail_market_divider_height)
    val footnoteTextSize = dimensionResource(R.dimen.type_detail_market_footnote_text_size).value.sp
    val footnoteLineHeight = dimensionResource(R.dimen.type_detail_market_footnote_line_height).value.sp
    val footnoteHorizontalPadding = dimensionResource(R.dimen.type_detail_market_footnote_horizontal_padding)
    val footnoteBottomPadding = dimensionResource(R.dimen.type_detail_market_footnote_bottom_padding)

    BaseContainer(
        title = stringResource(R.string.type_detail_section_market),
        useSystemBarsPadding = false,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MarketNavRow(
                label = stringResource(R.string.region_market),
                onClick = onOpenRegionMarket,
            )
            ItemDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cardHorizontalPadding, vertical = marketCardVerticalPadding),
            ) {
                MarketDepthPlaceholder(
                    title = stringResource(R.string.type_detail_market_buy_column_title),
                    caption = stringResource(R.string.type_detail_market_buy_caption),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .width(dividerThickness)
                        .height(marketDividerHeight)
                        .background(colorResource(R.color.border))
                        .align(Alignment.CenterVertically),
                )
                MarketDepthPlaceholder(
                    title = stringResource(R.string.type_detail_market_sell_column_title),
                    caption = stringResource(R.string.type_detail_market_sell_caption),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.type_detail_market_api_hint),
                fontSize = footnoteTextSize,
                lineHeight = footnoteLineHeight,
                color = colorResource(R.color.hint_text),
                modifier = Modifier.padding(
                    start = footnoteHorizontalPadding,
                    end = footnoteHorizontalPadding,
                    bottom = footnoteBottomPadding,
                ),
            )
        }
    }
}

@Composable
private fun MarketNavRow(
    label: String,
    onClick: () -> Unit,
) {
    val iconSize = dimensionResource(R.dimen.detail_row_icon_size)
    val iconGap = dimensionResource(R.dimen.detail_row_icon_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val labelTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Store,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = colorResource(R.color.text_primary),
            )
            Spacer(modifier = Modifier.width(iconGap))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.text_primary),
                fontSize = labelTextSize,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(chevronSize),
                tint = colorResource(R.color.hint_text),
            )
        }
    }
}

@Composable
private fun MarketDepthPlaceholder(
    title: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val statHorizontalPadding = dimensionResource(R.dimen.type_detail_market_stat_horizontal_padding)
    val footnoteTextSize = dimensionResource(R.dimen.type_detail_market_footnote_text_size).value.sp
    val statValueTextSize = dimensionResource(R.dimen.type_detail_market_stat_value_text_size).value.sp
    val statCaptionTextSize = dimensionResource(R.dimen.type_detail_market_stat_caption_text_size).value.sp
    val innerGapSmall = dimensionResource(R.dimen.type_detail_section_inner_gap_small)
    val trailingGap = dimensionResource(R.dimen.detail_row_trailing_gap)

    Column(
        modifier = modifier.padding(horizontal = statHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            fontSize = footnoteTextSize,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.hint_text),
        )
        Spacer(modifier = Modifier.height(innerGapSmall))
        Text(
            text = stringResource(R.string.type_detail_market_placeholder_value),
            fontSize = statValueTextSize,
            fontWeight = FontWeight.Medium,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(trailingGap))
        Text(
            text = caption,
            fontSize = statCaptionTextSize,
            color = colorResource(R.color.hint_text),
            textAlign = TextAlign.Center,
        )
    }
}
