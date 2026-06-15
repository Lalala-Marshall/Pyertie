package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

data class BaseSubMenuValueRowModel(
    val label: String,
    val value: String = "",
    val subMenuIndent: Boolean = true,
)

/** Read-only nested submenu row: indent, label, trailing value; no chevron. */
@Composable
fun BaseSubMenuValueRow(
    model: BaseSubMenuValueRowModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        BaseSubMenuValueRowContent(model = model)
        if (showDivider) ItemDivider()
    }
}

@Composable
fun BaseSubMenuValueRowContent(
    model: BaseSubMenuValueRowModel,
    modifier: Modifier = Modifier,
) {
    val leadingIndent = dimensionResource(R.dimen.sub_menu_leading_indent)
    val titleValueGap = dimensionResource(R.dimen.detail_row_title_value_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val labelTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val labelLineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    val valueTextSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (model.subMenuIndent) {
            Spacer(modifier = Modifier.width(leadingIndent))
        }

        Text(
            modifier = Modifier.weight(1f),
            text = model.label,
            color = colorResource(R.color.text_primary),
            fontSize = labelTextSize,
            lineHeight = labelLineHeight,
        )

        if (model.value.isNotEmpty()) {
            Spacer(modifier = Modifier.width(titleValueGap))

            Text(
                text = model.value,
                color = colorResource(R.color.hint_text),
                fontSize = valueTextSize,
            )
        }
    }
}
