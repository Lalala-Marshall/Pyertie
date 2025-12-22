package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.marshall.pyerite.R

@Composable
fun BaseLazyColumn(
    items: List<BaseLazyColumnItemModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(items) { index, item ->
            BaseLazyColumnItem(
                model = item,
                showDivider = index != items.lastIndex
            )
        }
    }
}

@Composable
fun BaseLazyColumnItem(
    model: BaseLazyColumnItemModel,
    showDivider: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        Icon(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(24.dp),
            painter = painterResource(model.iconRes),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(4.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

            if (showDivider) {
                ItemDivider()
            }
        }
    }
}

@Composable
private fun ItemDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth() ,
        thickness = 0.5.dp,
        color = colorResource(R.color.border),
    )
}

data class BaseLazyColumnItemModel(
    val iconRes: Int,
    val itemName: String,
)