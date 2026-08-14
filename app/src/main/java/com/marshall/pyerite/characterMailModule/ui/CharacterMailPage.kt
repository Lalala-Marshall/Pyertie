package com.marshall.pyerite.characterMailModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterMailModule.navHost.CharacterMailRoute
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed

@Composable
internal fun CharacterMailPage(
    navController: NavController,
    characterId: Long,
) {
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_mail)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
    ) { topBarPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding)
                .verticalScroll(scrollState)
                .padding(bottom = bottomPadding),
        ) {
            PageTitle(text = pageTitle)
            Spacer(modifier = Modifier.height(sectionGap))
            CharacterMailInboxSection(
                onAllMailClick = {
                    navController.navigate(CharacterMailRoute.All.create(characterId))
                },
            )
        }
    }
}

@Composable
private fun CharacterMailInboxSection(
    onAllMailClick: () -> Unit,
) {
    BaseContainer(
        title = null,
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                iconRes = R.drawable.ic_character_mail,
                itemName = stringResource(R.string.character_mail_all),
                onClick = onAllMailClick,
            ),
            showDivider = false,
        )
    }
}
