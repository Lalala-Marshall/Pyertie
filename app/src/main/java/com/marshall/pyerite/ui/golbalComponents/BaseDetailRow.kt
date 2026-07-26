package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marshall.pyerite.R
import java.io.File

/**
 * Label + trailing value row (no chevron). Thin wrapper over [BaseLazyColumnItem]
 * so type-detail dogma sections share the same padding / divider rules.
 */
@Composable
fun BaseDetailRow(
    model: BaseDetailRowModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val showLeadingIcon = model.iconFileName != null ||
        model.iconFile != null ||
        model.iconRes != R.drawable.ic_database
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = model.iconRes,
            iconFile = model.iconFile,
            iconFileName = model.iconFileName,
            iconOnLightPlate = model.iconOnLightPlate,
            showLeadingIcon = showLeadingIcon,
            itemName = model.label,
            trailingValue = model.value,
            showChevron = false,
            onClick = null,
        ),
        showDivider = showDivider,
        modifier = modifier,
    )
}

data class BaseDetailRowModel(
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val iconFileName: String? = null,
    val label: String,
    val value: String,
    /** Light plate behind dark SDE type icons (night mode contrast). */
    val iconOnLightPlate: Boolean = false,
) {
    companion object {
        private val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

        fun formatMappedValue(rawValue: Double?, unitName: String?): String {
            val value = rawValue ?: 0.0
            if (unitName != null && unitName.contains("=")) {
                val mapping = unitName.split(Regex("\\s+"))
                    .mapNotNull { it.trim().split("=").takeIf { parts -> parts.size == 2 } }
                    .associate { it[0].trim() to it[1].trim() }

                val key = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
                mapping[key]?.let { return it }
            }
            return "${formatter.format(value)} ${unitName ?: ""}".trim()
        }
    }
}
