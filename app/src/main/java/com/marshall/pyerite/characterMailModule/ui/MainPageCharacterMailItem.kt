package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemLayout
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.baseLazyColumnItemAdaptiveIconSize

/**
 * Home Character-section rows (sheet / clones / skills) each have a hint line.
 * Mail is title-only, so icon size must be forced to that same line count.
 */
private const val HOME_CHARACTER_SECTION_HINT_LINE_COUNT = 1

/** Home-page EVE Mail row (under skills inside the Character section card). */
@Composable
fun MainPageCharacterMailItem(
    onClick: () -> Unit,
    showDivider: Boolean = false,
) {
    val iconSize = baseLazyColumnItemAdaptiveIconSize(
        contentLineCount = BaseLazyColumnItemLayout.TITLE_LINE_COUNT +
            HOME_CHARACTER_SECTION_HINT_LINE_COUNT,
    )
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconRes = R.drawable.ic_character_mail,
            iconSize = iconSize,
            itemName = stringResource(R.string.character_mail),
            onClick = onClick,
        ),
        showDivider = showDivider,
    )
}
