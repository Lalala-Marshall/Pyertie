package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseContainer

@Composable
fun TypeDetailVariantsSection(
    variantCount: Int,
    onBrowseVariants: () -> Unit,
) {
    if (variantCount <= 1) return

    BaseContainer(
        title = stringResource(R.string.type_detail_variants_section),
        useSystemBarsPadding = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBrowseVariants),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.type_detail_browse_variants, variantCount),
                    color = colorResource(R.color.text_primary),
                    fontSize = 16.sp,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = colorResource(R.color.hint_text),
                )
            }
        }
    }
}
