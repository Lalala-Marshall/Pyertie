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
import java.io.File

@Composable
fun BaseDetailRow(
    model: BaseDetailRowModel,
    showDivider: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = model.iconFile?.let { file ->
                    rememberAsyncImagePainter(file)
                } ?: painterResource(model.iconRes),
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

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
    val label: String,
    val value: String
)
