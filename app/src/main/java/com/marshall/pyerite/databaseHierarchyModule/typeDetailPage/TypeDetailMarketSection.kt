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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                MarketDepthPlaceholder(
                    title = stringResource(R.string.type_detail_market_buy_column_title),
                    caption = stringResource(R.string.type_detail_market_buy_caption),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .width(0.5.dp)
                        .height(64.dp)
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
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colorResource(R.color.hint_text),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun MarketNavRow(
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Store,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorResource(R.color.text_primary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.text_primary),
                fontSize = 16.sp,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
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
    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.hint_text),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.type_detail_market_placeholder_value),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = colorResource(R.color.text_primary),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = caption,
            fontSize = 11.sp,
            color = colorResource(R.color.hint_text),
            textAlign = TextAlign.Center,
        )
    }
}
