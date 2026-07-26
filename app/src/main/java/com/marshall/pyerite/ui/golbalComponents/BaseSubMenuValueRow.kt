package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            leadingIndent = model.subMenuIndent,
            itemName = model.label,
            trailingValue = model.value,
            showChevron = false,
            onClick = null,
        ),
        showDivider = showDivider,
        modifier = modifier,
    )
}
