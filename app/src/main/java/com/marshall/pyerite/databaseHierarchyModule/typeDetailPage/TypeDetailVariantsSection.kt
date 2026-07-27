package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

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
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.type_detail_browse_variants, variantCount),
                showChevron = true,
                onClick = onBrowseVariants,
            ),
            showDivider = false,
        )
    }
}
