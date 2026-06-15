package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import com.marshall.pyerite.data.icons.IconManager
import org.koin.compose.koinInject
import java.io.File

@Composable
fun BaseDetailRow(
    model: BaseDetailRowModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
    iconManager: IconManager = koinInject()
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shouldShowIcon = model.iconFileName != null ||
                model.iconFile != null ||
                model.iconRes != R.drawable.ic_database
            if (shouldShowIcon) {
                val painter = when {
                    model.iconFileName != null -> {
                        rememberAsyncImagePainter(iconManager.getIconFile(model.iconFileName))
                    }
                    model.iconFile != null -> {
                        rememberAsyncImagePainter(model.iconFile)
                    }
                    else -> {
                        painterResource(model.iconRes)
                    }
                }

                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painter,
                    contentDescription = null,
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                modifier = Modifier.weight(1f),
                text = model.label,
                color = colorResource(R.color.text_primary),
            )

            Text(
                text = model.value,
                color = colorResource(R.color.hint_text),
                fontSize = 14.sp
            )
        }

        if (showDivider) ItemDivider()
    }
}

data class BaseDetailRowModel(
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val iconFileName: String? = null,
    val label: String,
    val value: String
) {
    companion object {
        private val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

        fun formatMappedValue(rawValue: Double?, unitName: String?): String {
            val value = rawValue ?: 0.0
            if (unitName != null && unitName.contains("=")) {
                // Handle "1=xxx 2=yyy 3=zzz" mapping
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
