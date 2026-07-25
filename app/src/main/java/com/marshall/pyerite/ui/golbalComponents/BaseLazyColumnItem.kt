package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import java.io.File

@Composable
fun BaseLazyColumnItem(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
) {
    val iconSize = dimensionResource(R.dimen.detail_row_icon_size)
    val iconGap = dimensionResource(R.dimen.detail_row_icon_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val hintTextSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp
    val hintLineHeight = dimensionResource(R.dimen.detail_row_label_subtitle_line_height).value.sp
    val hintSpacing = dimensionResource(R.dimen.detail_row_label_subtitle_spacing)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { model.onClick() }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = rowHorizontalPadding, vertical = rowVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = model.iconFile?.let { file ->
                    rememberAsyncImagePainter(file)
                } ?: painterResource(model.iconRes),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(iconGap))

            if (model.itemHint.isNotEmpty()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(hintSpacing),
                ) {
                    Text(
                        text = model.itemName,
                        color = colorResource(R.color.text_primary),
                    )
                    Text(
                        text = model.itemHint,
                        color = colorResource(R.color.hint_text),
                        fontSize = hintTextSize,
                        lineHeight = hintLineHeight,
                    )
                }
            } else {
                Text(
                    modifier = Modifier.weight(1f),
                    text = model.itemName,
                    color = colorResource(R.color.text_primary),
                )
            }

            Icon(
                modifier = Modifier.size(chevronSize),
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorResource(R.color.hint_text),
            )
        }

        if (showDivider) ItemDivider()
    }
}

@Composable
fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dimensionResource(R.dimen.detail_divider_start_padding)),
        thickness = dimensionResource(R.dimen.detail_divider_thickness),
        color = colorResource(R.color.border),
    )
}

data class BaseLazyColumnItemModel(
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val itemName: String,
    /** Secondary line under [itemName] (e.g. skill-point summary). */
    val itemHint: String = "",
    val onClick: () -> Unit,
)
