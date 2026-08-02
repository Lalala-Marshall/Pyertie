package com.marshall.pyerite.characterSkillsModule.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillPoints
import com.marshall.pyerite.characterSkillsModule.model.CharacterSkillQueueStatus
import com.marshall.pyerite.characterSkillsModule.model.SkillInjectorConfig
import com.marshall.pyerite.characterSkillsModule.model.SkillInjectorSize
import com.marshall.pyerite.characterSkillsModule.model.SkillQueueInjectorNeeds
import com.marshall.pyerite.localization.LocaleController
import com.marshall.pyerite.localization.displayName
import com.marshall.pyerite.sdeModule.room.RoomProvider
import com.marshall.pyerite.sdeModule.room.type.TypeEntity
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.util.NumberDisplayFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
internal fun CharacterSkillsQueueInjectorsSection(
    status: CharacterSkillQueueStatus,
    skillPoints: CharacterSkillPoints,
    nowMs: Long,
    localeController: LocaleController,
    onInjectorClick: (Int) -> Unit,
    roomProvider: RoomProvider = koinInject(),
) {
    val largeType by produceState<TypeEntity?>(
        initialValue = null,
        key1 = localeController.contentLanguage,
    ) {
        value = withContext(Dispatchers.IO) {
            roomProvider.getDatabase().typeDao()
                .getTypeById(SkillInjectorConfig.LARGE_TYPE_ID)
        }
    }
    val smallType by produceState<TypeEntity?>(
        initialValue = null,
        key1 = localeController.contentLanguage,
    ) {
        value = withContext(Dispatchers.IO) {
            roomProvider.getDatabase().typeDao()
                .getTypeById(SkillInjectorConfig.SMALL_TYPE_ID)
        }
    }

    // Nexus: estimated SP = raw queue remaining (do not subtract unallocated).
    val requiredSp = remember(status.queuedEntries, nowMs) {
        SkillQueueInjectorNeeds.remainingQueueSp(
            entries = status.queuedEntries,
            nowMs = nowMs,
        )
    }
    val injectorTierSp = skillPoints.totalSpIncludingUnallocated
    val mix = remember(requiredSp, injectorTierSp) {
        SkillQueueInjectorNeeds.injectorMixNeeded(
            gapSp = requiredSp,
            totalSp = injectorTierSp,
        )
    }
    val largeYield = remember(injectorTierSp) {
        SkillQueueInjectorNeeds.yieldAt(
            totalSp = injectorTierSp,
            size = SkillInjectorSize.LARGE,
        )
    }
    val smallYield = remember(injectorTierSp) {
        SkillQueueInjectorNeeds.yieldAt(
            totalSp = injectorTierSp,
            size = SkillInjectorSize.SMALL,
        )
    }
    val requiredSpText = NumberDisplayFormatter.format(
        requiredSp,
        NumberDisplayFormatter.Style.FULL,
    )

    BaseContainer(
        title = stringResource(R.string.character_skills_queue_injectors_section),
        useSystemBarsPadding = false,
    ) {
        if (mix.largeCount > 0) {
            InjectorNeedRow(
                type = largeType,
                fallbackName = stringResource(R.string.character_skills_queue_injector_large_fallback),
                count = mix.largeCount,
                yieldSp = largeYield,
                localeController = localeController,
                onClick = { onInjectorClick(SkillInjectorConfig.LARGE_TYPE_ID) },
            )
        }
        if (mix.smallCount > 0) {
            InjectorNeedRow(
                type = smallType,
                fallbackName = stringResource(R.string.character_skills_queue_injector_small_fallback),
                count = mix.smallCount,
                yieldSp = smallYield,
                localeController = localeController,
                onClick = { onInjectorClick(SkillInjectorConfig.SMALL_TYPE_ID) },
            )
        }
        Text(
            text = stringResource(
                R.string.character_skills_queue_injectors_estimated_sp,
                requiredSpText,
            ),
            color = colorResource(R.color.hint_text),
            fontSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp,
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.detail_row_horizontal_padding),
                vertical = dimensionResource(R.dimen.detail_row_vertical_padding_multi_line),
            ),
        )
    }
}

@Composable
private fun InjectorNeedRow(
    type: TypeEntity?,
    fallbackName: String,
    count: Int,
    yieldSp: Long,
    localeController: LocaleController,
    onClick: () -> Unit,
) {
    val name = type?.displayName(localeController)?.takeIf { it.isNotBlank() } ?: fallbackName
    val countText = NumberDisplayFormatter.format(
        count.toLong(),
        NumberDisplayFormatter.Style.FULL,
    )
    val yieldText = NumberDisplayFormatter.format(
        yieldSp,
        NumberDisplayFormatter.Style.FULL,
    )
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconFileName = type?.iconFilename,
            showLeadingIcon = !type?.iconFilename.isNullOrBlank(),
            itemName = name,
            itemHint = stringResource(
                R.string.character_skills_queue_injector_yield,
                yieldText,
            ),
            trailingValue = countText,
            onClick = onClick,
        ),
        showDivider = true,
    )
}
