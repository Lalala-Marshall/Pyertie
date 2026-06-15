package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BaseLazyColumn(
    items: List<BaseLazyColumnItemModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        itemsIndexed(items) { index, item ->
            BaseLazyColumnItem(
                model = item,
                showDivider = index != items.lastIndex
            )
        }
    }
}