package com.marshall.pyerite.characterClonesModule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterClonesModule.model.ActiveImplantInfo
import com.marshall.pyerite.characterClonesModule.model.JumpCloneAtLocation
import com.marshall.pyerite.characterClonesModule.viewModel.CharacterClonesViewModel
import com.marshall.pyerite.databaseHierarchyModule.navHost.DatabaseRoute
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CharacterClonesLocationPage(
    navController: NavController,
    viewModel: CharacterClonesViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_clone_detail)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val location = uiState.selectedLocation
    val clones = location?.clones.orEmpty()

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
            when {
                location == null -> {
                    BaseContainer(useSystemBarsPadding = false) {
                        BaseLazyColumnItem(
                            model = BaseLazyColumnItemModel(
                                showLeadingIcon = false,
                                itemName = placeholder,
                                showChevron = false,
                                onClick = null,
                            ),
                            showDivider = false,
                        )
                    }
                }
                clones.isEmpty() -> {
                    BaseContainer(useSystemBarsPadding = false) {
                        BaseLazyColumnItem(
                            model = BaseLazyColumnItemModel(
                                showLeadingIcon = false,
                                itemName = stringResource(R.string.character_clone_jump_clones_empty),
                                showChevron = false,
                                onClick = null,
                            ),
                            showDivider = false,
                        )
                    }
                }
                else -> {
                    clones.forEachIndexed { index, clone ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(sectionGap))
                        }
                        JumpCloneImplantsSection(
                            clone = clone,
                            onImplantClick = { typeId ->
                                navController.navigate(DatabaseRoute.TypeDetail.create(typeId))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JumpCloneImplantsSection(
    clone: JumpCloneAtLocation,
    onImplantClick: (typeId: Int) -> Unit,
    localeController: LocaleController = koinInject(),
) {
    BaseContainer(
        title = stringResource(R.string.character_clone_id_section, clone.jumpCloneId),
        useSystemBarsPadding = false,
    ) {
        CloneImplantList(
            implants = clone.implants,
            localeController = localeController,
            onImplantClick = onImplantClick,
        )
    }
}

@Composable
private fun CloneImplantList(
    implants: List<ActiveImplantInfo>,
    localeController: LocaleController,
    onImplantClick: (typeId: Int) -> Unit,
) {
    if (implants.isEmpty()) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_clone_no_implants),
                showChevron = false,
                onClick = null,
            ),
            showDivider = false,
        )
        return
    }
    Column {
        implants.forEachIndexed { index, implant ->
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_database,
                    iconFileName = implant.iconFilename,
                    iconOnLightPlate = true,
                    itemName = implant.displayName(localeController),
                    showChevron = true,
                    onClick = { onImplantClick(implant.typeId) },
                ),
                showDivider = index != implants.lastIndex,
            )
        }
    }
}
