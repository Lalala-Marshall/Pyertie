package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Composable
fun PageTitle(
    text: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    val pageTitleTextSize = dimensionResource(R.dimen.list_page_title_text_size).value.sp
    val titleStartPadding = dimensionResource(R.dimen.type_detail_page_title_start_padding)
    val titleVerticalPadding = dimensionResource(R.dimen.type_detail_page_title_vertical_padding)

    if (leadingContent != null) {
        Row(
            modifier = modifier.padding(
                start = titleStartPadding,
                top = titleVerticalPadding,
                bottom = dimensionResource(R.dimen.list_page_title_bottom_padding),
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()
            Text(
                text = text,
                fontSize = pageTitleTextSize,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
            )
        }
    } else {
        Text(
            text = text,
            fontSize = pageTitleTextSize,
            fontWeight = FontWeight.Black,
            color = colorResource(R.color.text_primary),
            modifier = modifier.padding(
                start = titleStartPadding,
                top = titleVerticalPadding,
                bottom = dimensionResource(R.dimen.list_page_title_bottom_padding),
            ),
        )
    }
}
