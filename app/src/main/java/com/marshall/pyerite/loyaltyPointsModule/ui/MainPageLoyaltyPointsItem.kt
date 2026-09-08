package com.marshall.pyerite.loyaltyPointsModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemLayout
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.baseLazyColumnItemAdaptiveIconSize

private const val HOME_CHARACTER_SECTION_HINT_LINE_COUNT = 1

@Composable
fun MainPageLoyaltyPointsItem(
    onClick: () -> Unit,
    showDivider: Boolean = false,
) {
    val iconSize = baseLazyColumnItemAdaptiveIconSize(
        contentLineCount = BaseLazyColumnItemLayout.TITLE_LINE_COUNT +
            HOME_CHARACTER_SECTION_HINT_LINE_COUNT,
    )
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_loyalty_store,
            iconSize = iconSize,
            itemName = stringResource(R.string.loyalty_points),
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}
