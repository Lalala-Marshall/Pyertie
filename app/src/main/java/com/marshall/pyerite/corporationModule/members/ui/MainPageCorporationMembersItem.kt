package com.marshall.pyerite.corporationModule.members.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
fun MainPageCorporationMembersItem(
    onClick: () -> Unit,
    showDivider: Boolean = false,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_corporation_members,
            itemName = stringResource(R.string.corporation_members),
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}
