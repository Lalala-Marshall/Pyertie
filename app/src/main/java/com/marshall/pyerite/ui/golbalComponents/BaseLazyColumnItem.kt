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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.marshall.pyerite.R
import java.io.File

@Composable
fun BaseLazyColumnItem(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { model.onClick() }
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
                } ?: painterResource(model.iconRes) ,
                contentDescription = null,
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = model.itemName,
                color = colorResource(R.color.text_primary),
            )

            Icon(
                modifier = Modifier.size(20.dp),
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
    val onClick: () -> Unit
)
