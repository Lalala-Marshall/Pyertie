package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marshall.pyerite.R
import com.marshall.pyerite.iconModule.manager.IconManager
import org.koin.compose.koinInject
import java.io.File

data class BaseSubMenuRowModel(
    val label: String,
    /** Secondary line under [label], left-aligned (e.g. success rate caption). */
    val labelHint: String = "",
    val value: String = "",
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val iconFileName: String? = null,
    /** When true, reserves leading space so the row reads as a nested submenu item. */
    val subMenuIndent: Boolean = true,
    val onClick: () -> Unit = {},
)

/**
 * Nested / navigable submenu row. Thin wrapper over [BaseLazyColumnItem].
 */
@Composable
fun BaseSubMenuRow(
    model: BaseSubMenuRowModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
    iconManager: IconManager = koinInject(),
) {
    val hasIconFile = model.iconFile != null ||
        model.iconFileName?.let { iconManager.getIconFile(it) } != null
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = model.iconRes,
            iconFile = model.iconFile,
            iconFileName = model.iconFileName,
            showLeadingIcon = hasIconFile || model.iconRes != R.drawable.ic_database,
            leadingIndent = model.subMenuIndent,
            itemName = model.label,
            itemHint = model.labelHint,
            trailingValue = model.value,
            showChevron = true,
            onClick = model.onClick,
        ),
        showDivider = showDivider,
        modifier = modifier,
        iconManager = iconManager,
    )
}
