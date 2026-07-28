package com.marshall.pyerite.characterSkillsModule.ui

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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterAttributes
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueConfig
import com.marshall.pyerite.characterSkillsModule.viewModel.CharacterSkillsViewModel
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.PageTitle
import com.marshall.pyerite.ui.golbalComponents.PyeritePageScaffold
import com.marshall.pyerite.ui.golbalComponents.PyeritePullToRefreshBox
import com.marshall.pyerite.ui.golbalComponents.pyeritePullRefreshTopBarAction
import com.marshall.pyerite.ui.golbalComponents.rememberNavigateUpAction
import com.marshall.pyerite.ui.golbalComponents.rememberScrollTitleCollapsed
import com.marshall.pyerite.util.formatDurationDisplay
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun CharacterAttributesPage(
    navController: NavController,
    viewModel: CharacterSkillsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val pageTitle = stringResource(R.string.character_skills_attributes)
    val onBack = navController.rememberNavigateUpAction()
    val showCollapsedTitle = rememberScrollTitleCollapsed(scrollState)
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val endActions = listOfNotNull(
        pyeritePullRefreshTopBarAction(
            isRefreshing = uiState.isLoading,
            refreshFailed = uiState.loadFailed,
            onRefresh = viewModel::refresh,
        ),
    )

    PyeritePageScaffold(
        title = pageTitle,
        showCollapsedTitle = showCollapsedTitle,
        onBack = onBack,
        endActions = endActions,
    ) { topBarPadding ->
        PyeritePullToRefreshBox(
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(topBarPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = bottomPadding),
            ) {
                PageTitle(text = pageTitle)
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterAttributesBaseSection(
                    attributes = uiState.attributes,
                    detailsPending = !uiState.attributesReady,
                    placeholder = placeholder,
                )
                Spacer(modifier = Modifier.height(sectionGap))
                CharacterAttributesRemapSection(
                    attributes = uiState.attributes,
                    detailsPending = !uiState.attributesReady,
                    placeholder = placeholder,
                )
            }
        }
    }
}

@Composable
private fun CharacterAttributesBaseSection(
    attributes: CharacterAttributes,
    detailsPending: Boolean,
    placeholder: String,
) {
    val rows = listOf(
        Triple(
            R.drawable.ic_attr_perception,
            R.string.character_skills_attr_perception,
            attributes.perception,
        ),
        Triple(
            R.drawable.ic_attr_memory,
            R.string.character_skills_attr_memory,
            attributes.memory,
        ),
        Triple(
            R.drawable.ic_attr_willpower,
            R.string.character_skills_attr_willpower,
            attributes.willpower,
        ),
        Triple(
            R.drawable.ic_attr_intelligence,
            R.string.character_skills_attr_intelligence,
            attributes.intelligence,
        ),
        Triple(
            R.drawable.ic_attr_charisma,
            R.string.character_skills_attr_charisma,
            attributes.charisma,
        ),
    )

    BaseContainer(
        title = stringResource(R.string.character_skills_base_attributes_section),
        useSystemBarsPadding = false,
    ) {
        rows.forEachIndexed { index, (iconRes, nameRes, value) ->
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    iconRes = iconRes,
                    itemName = stringResource(nameRes),
                    trailingValue = if (detailsPending) placeholder else value.toString(),
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = index != rows.lastIndex,
            )
        }
    }
}

@Composable
private fun CharacterAttributesRemapSection(
    attributes: CharacterAttributes,
    detailsPending: Boolean,
    placeholder: String,
) {
    val bonusRemapsValue = if (detailsPending) {
        placeholder
    } else {
        attributes.bonusRemaps.toString()
    }

    BaseContainer(
        title = stringResource(R.string.character_skills_remap_section),
        useSystemBarsPadding = false,
    ) {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_skills_bonus_remaps),
                trailingValue = bonusRemapsValue,
                showChevron = false,
                onClick = null,
            ),
            showDivider = true,
        )
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(R.string.character_skills_next_remap),
                trailingValue = nextRemapCountdownValue(
                    nextRemapAvailableEpochMs = attributes.nextRemapAvailableEpochMs,
                    detailsPending = detailsPending,
                    placeholder = placeholder,
                ),
                showChevron = false,
                onClick = null,
            ),
            showDivider = false,
        )
    }
}

@Composable
private fun nextRemapCountdownValue(
    nextRemapAvailableEpochMs: Long?,
    detailsPending: Boolean,
    placeholder: String,
): String {
    if (detailsPending) return placeholder
    val readyNow = stringResource(R.string.character_clone_jump_now)
    val targetMs = nextRemapAvailableEpochMs ?: return readyNow
    val nowMs by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = targetMs,
    ) {
        while (isActive) {
            value = System.currentTimeMillis()
            if (value >= targetMs) break
            delay(CharacterSkillQueueConfig.UI_TICK_MS.milliseconds)
        }
    }
    return if (nowMs >= targetMs) {
        readyNow
    } else {
        stringResource(
            R.string.character_skills_next_remap_in,
            formatDurationDisplay(
                totalSeconds = (targetMs - nowMs) / CharacterSkillQueueConfig.MILLIS_PER_SECOND,
                includeSeconds = false,
            ),
        )
    }
}
