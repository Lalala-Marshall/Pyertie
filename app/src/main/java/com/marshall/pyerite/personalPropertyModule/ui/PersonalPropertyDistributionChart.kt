package com.marshall.pyerite.personalPropertyModule.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.util.NumberDisplayFormatter

internal data class PersonalPropertySliceUi(
    val label: String,
    val isk: Double?,
    val color: Color,
)

@Composable
internal fun PersonalPropertyDistributionChart(
    slices: List<PersonalPropertySliceUi>,
    detailsReady: Boolean,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val chartSize = dimensionResource(R.dimen.personal_property_chart_size)
    val strokeWidth = dimensionResource(R.dimen.personal_property_chart_stroke)
    val legendGap = dimensionResource(R.dimen.personal_property_legend_row_gap)
    val chartGap = dimensionResource(R.dimen.personal_property_chart_legend_gap)
    val contentPadding = dimensionResource(R.dimen.personal_property_distribution_padding)
    val emptyRingColor = colorResource(R.color.border)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(legendGap),
        ) {
            slices.forEach { slice ->
                DistributionLegendRow(
                    slice = slice,
                    detailsReady = detailsReady,
                    placeholder = placeholder,
                )
            }
        }
        Spacer(modifier = Modifier.width(chartGap))
        Canvas(modifier = Modifier.size(chartSize)) {
            val ringWidth = strokeWidth.toPx()
            val inset = ringWidth / 2f
            val arcSize = Size(
                width = size.minDimension - ringWidth,
                height = size.minDimension - ringWidth,
            )
            val topLeft = Offset(inset, inset)
            val stroke = Stroke(width = ringWidth, cap = StrokeCap.Butt)
            val positive = slices.mapNotNull { slice ->
                val value = slice.isk
                if (value != null && value > 0.0) slice to value else null
            }
            val total = positive.sumOf { it.second }
            if (total <= 0.0) {
                drawArc(
                    color = emptyRingColor,
                    startAngle = 0f,
                    sweepAngle = PersonalPropertyChartConfig.FULL_CIRCLE_DEGREES,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                return@Canvas
            }
            var startAngle = PersonalPropertyChartConfig.START_ANGLE_DEGREES
            positive.forEachIndexed { index, (slice, value) ->
                val sweep = if (index == positive.lastIndex) {
                    PersonalPropertyChartConfig.FULL_CIRCLE_DEGREES - startAngle +
                        PersonalPropertyChartConfig.START_ANGLE_DEGREES
                } else {
                    (value / total * PersonalPropertyChartConfig.FULL_CIRCLE_DEGREES).toFloat()
                }
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun DistributionLegendRow(
    slice: PersonalPropertySliceUi,
    detailsReady: Boolean,
    placeholder: String,
) {
    val dotSize = dimensionResource(R.dimen.personal_property_legend_dot_size)
    val dotGap = dimensionResource(R.dimen.personal_property_legend_dot_gap)
    val value = formatPropertyIsk(slice.isk, detailsReady, placeholder)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color = slice.color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.width(dotGap))
        Text(
            text = slice.label,
            color = colorResource(R.color.text_primary),
            fontSize = dimensionResource(R.dimen.personal_property_legend_label_text_size).value.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(dotGap))
        Text(
            text = value,
            color = colorResource(R.color.hint_text),
            fontSize = dimensionResource(R.dimen.personal_property_legend_value_text_size).value.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

internal fun formatPropertyIsk(
    value: Double?,
    detailsReady: Boolean,
    placeholder: String,
): String {
    if (!detailsReady || value == null) return placeholder
    return NumberDisplayFormatter.format(value, NumberDisplayFormatter.Style.COMPACT)
}

private object PersonalPropertyChartConfig {
    const val FULL_CIRCLE_DEGREES = 360f
    const val START_ANGLE_DEGREES = -90f
}
