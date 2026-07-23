package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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

private val DetailRowLabelFontSize = 16.sp
private val DetailRowValueFontSize = 14.sp
private val DetailRowLabelSubtitleFontSize = 12.sp

/**
 * [BaseDetailRowModel] with a second label line (smaller, lighter caption under [label]).
 */
data class BaseDetailRowSubtitleModel(
    val iconRes: Int = R.drawable.ic_database,
    val iconFile: File? = null,
    val iconFileName: String? = null,
    val label: String,
    val labelSubtitle: String,
    val value: String,
) {

    companion object {
        fun from(base: BaseDetailRowModel, labelSubtitle: String): BaseDetailRowSubtitleModel =
            BaseDetailRowSubtitleModel(
                iconRes = base.iconRes,
                iconFile = base.iconFile,
                iconFileName = base.iconFileName,
                label = base.label,
                labelSubtitle = labelSubtitle,
                value = base.value,
            )
    }
}

@Composable
fun BaseDetailSubtitleRow(
    model: BaseDetailRowSubtitleModel,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
    iconManager: IconManager = koinInject(),
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shouldShowIcon = model.iconFileName != null ||
                model.iconFile != null ||
                model.iconRes != R.drawable.ic_database
            if (shouldShowIcon) {
                val resolvedFile = model.iconFile
                    ?: model.iconFileName?.let { iconManager.getIconFile(it) }
                val painter = when {
                    resolvedFile != null -> rememberAsyncImagePainter(resolvedFile)
                    else -> painterResource(model.iconRes)
                }

                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painter,
                    contentDescription = null,
                    tint = Color.Unspecified,
                )

                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text = model.label,
                    color = colorResource(R.color.text_primary),
                    fontSize = DetailRowLabelFontSize,
                    lineHeight = DetailRowLabelFontSize,
                )
                Text(
                    text = model.labelSubtitle,
                    color = colorResource(R.color.text_caption),
                    fontSize = DetailRowLabelSubtitleFontSize,
                    lineHeight = DetailRowLabelSubtitleFontSize,
                )
            }

            Text(
                text = model.value,
                color = colorResource(R.color.hint_text),
                fontSize = DetailRowValueFontSize,
            )
        }

        if (showDivider) ItemDivider()
    }
}
