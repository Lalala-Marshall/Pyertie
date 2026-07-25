package com.marshall.pyerite.characterSheetModule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

/** Home-page entry row under the Character section. */
@Composable
fun MainPageCharacterSheetItem(
    skillPointsHint: String,
    onClick: () -> Unit,
) {
    val cardCornerRadius = dimensionResource(R.dimen.detail_card_corner_radius)
    val shape = RoundedCornerShape(cardCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding))
            .padding(bottom = dimensionResource(R.dimen.character_main_card_bottom_spacing))
            .clip(shape)
            .background(colorResource(R.color.second_background), shape),
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_sheet,
                itemName = stringResource(R.string.character_sheet),
                itemHint = skillPointsHint,
                onClick = onClick,
            ),
            showDivider = false,
        )
    }
}
