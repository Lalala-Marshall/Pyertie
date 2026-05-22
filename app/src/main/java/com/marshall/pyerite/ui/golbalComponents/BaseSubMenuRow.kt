package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import org.koin.compose.koinInject
import java.io.File

data class BaseSubMenuRowModel(
    val label: String,
    val value: String = "",
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val iconFileName: String? = null,
    /** When true, reserves leading space so the row reads as a nested submenu item. */
    val subMenuIndent: Boolean = true,
    val onClick: () -> Unit = {},
)

/**
 * List row for nested submenu entries: optional leading indent, icon, wrapping title,
 * trailing value, and chevron. Use inside cards/sections with [showDivider] between items.
 */
@Composable
fun BaseSubMenuRow(
    model: BaseSubMenuRowModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
    iconManager: IconManager = koinInject(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = model.onClick),
    ) {
        BaseSubMenuRowContent(model = model, iconManager = iconManager)
        if (showDivider) ItemDivider()
    }
}

@Composable
fun BaseSubMenuRowContent(
    model: BaseSubMenuRowModel,
    modifier: Modifier = Modifier,
    iconManager: IconManager = koinInject(),
) {
    val iconSize = dimensionResource(R.dimen.detail_row_icon_size)
    val iconGap = dimensionResource(R.dimen.detail_row_icon_gap)
    val leadingIndent = dimensionResource(R.dimen.sub_menu_leading_indent)
    val titleValueGap = dimensionResource(R.dimen.detail_row_title_value_gap)
    val rowHorizontalPadding = dimensionResource(R.dimen.detail_row_horizontal_padding)
    val rowVerticalPadding = dimensionResource(R.dimen.detail_row_vertical_padding)
    val chevronSize = dimensionResource(R.dimen.detail_row_chevron_size)
    val trailingGap = dimensionResource(R.dimen.detail_row_trailing_gap)
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

        SubMenuRowLeadingIcon(
            iconSize = iconSize,
            iconGap = iconGap,
            iconFile = model.iconFile,
            iconFileName = model.iconFileName,
            iconManager = iconManager,
        )

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

        Spacer(modifier = Modifier.width(trailingGap))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(chevronSize),
            tint = colorResource(R.color.hint_text),
        )
    }
}

@Composable
private fun SubMenuRowLeadingIcon(
    iconSize: Dp,
    iconGap: Dp,
    iconFile: File?,
    iconFileName: String?,
    iconManager: IconManager,
) {
    val resolvedFile = iconFile ?: iconFileName?.let { iconManager.getIconFile(it) } ?: return

    Icon(
        modifier = Modifier.size(iconSize),
        painter = rememberAsyncImagePainter(resolvedFile),
        contentDescription = null,
        tint = Color.Unspecified,
    )
    Spacer(modifier = Modifier.width(iconGap))
}
