package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

data class PyeriteSegmentedOption<T>(
    val value: T,
    val label: String,
)

/**
 * Equal-width pill segments (skill-catalog filter, entity-profile tabs, etc.).
 */
@Composable
fun <T> PyeriteSegmentedControl(
    options: List<PyeriteSegmentedOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val shape = RoundedCornerShape(barHeight / 2)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.segmented_control_outer_horizontal_padding),
                vertical = dimensionResource(R.dimen.segmented_control_outer_vertical_padding),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colorResource(R.color.search_field_idle_background), shape)
                .padding(
                    horizontal = dimensionResource(R.dimen.segmented_control_bar_horizontal_padding),
                    vertical = dimensionResource(R.dimen.segmented_control_bar_vertical_padding),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { option ->
                val isSelected = option.value == selected
                val itemShape = RoundedCornerShape(barHeight / 2)
                Text(
                    text = option.label,
                    color = if (isSelected) {
                        colorResource(R.color.text_primary)
                    } else {
                        colorResource(R.color.hint_text)
                    },
                    fontSize = dimensionResource(R.dimen.segmented_control_text_size).value.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .weight(1f)
                        .clip(itemShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    colorResource(R.color.segmented_control_selected),
                                    itemShape,
                                )
                            } else {
                                Modifier
                            },
                        )
                        .clickable(onClick = { onSelect(option.value) })
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.segmented_control_item_horizontal_padding,
                            ),
                            vertical = dimensionResource(
                                R.dimen.segmented_control_item_vertical_padding,
                            ),
                        ),
                )
            }
        }
    }
}
