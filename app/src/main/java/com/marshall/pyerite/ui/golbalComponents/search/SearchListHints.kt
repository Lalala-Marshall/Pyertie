package com.marshall.pyerite.ui.golbalComponents.search

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Composable
fun SearchNoResultsItem(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.search_no_results),
        modifier = modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            top = dimensionResource(R.dimen.type_detail_section_gap),
            end = dimensionResource(R.dimen.detail_card_horizontal_padding),
        ),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = colorResource(R.color.hint_text),
    )
}

@Composable
fun SearchResultsTruncatedItem(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        modifier = modifier.padding(
            start = dimensionResource(R.dimen.type_detail_page_title_start_padding),
            top = dimensionResource(R.dimen.type_detail_section_gap),
            end = dimensionResource(R.dimen.detail_card_horizontal_padding),
        ),
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = colorResource(R.color.hint_text),
    )
}
