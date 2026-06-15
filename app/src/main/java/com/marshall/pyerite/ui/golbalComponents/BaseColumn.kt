package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BaseColumn(
    items: List<BaseLazyColumnItemModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            BaseLazyColumnItem(
                model = item,
                showDivider = index != items.lastIndex
            )
        }
    }
}
