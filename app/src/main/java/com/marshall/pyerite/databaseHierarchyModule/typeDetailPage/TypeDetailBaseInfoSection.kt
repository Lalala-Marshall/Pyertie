package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.databaseHierarchyModule.room.entity.DogmaAttributeEntity
import com.marshall.pyerite.databaseHierarchyModule.room.entity.TypeEntity
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRow
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel
import com.marshall.pyerite.ui.golbalComponents.BaseDetailRowModel.Companion.formatMappedValue

internal val TypeDetailBaseInfoSectionDogmaNames = listOf(
    TypeEntity::volume.name,
    TypeEntity::capacity.name,
    TypeEntity::mass.name,
)

private data class BaseInfoRowSpec(
    val dogmaName: String,
    val value: (TypeEntity) -> Double?,
    val labelOverride: String? = null,
    val iconFileNameOverride: String? = null,
)

@Composable
fun TypeDetailBaseInfoSection(
    entity: TypeEntity,
    dogmaAttributes: List<DogmaAttributeEntity>,
) {
    val dogmaByName = remember(dogmaAttributes) { dogmaAttributes.associateBy { it.name } }
    val repackagedVolumeLabel = stringResource(R.string.repackaged_volume)
    val detailItems = remember(
        entity,
        dogmaByName,
        repackagedVolumeLabel,
    ) {
        listOf(
            BaseInfoRowSpec(
                dogmaName = TypeEntity::volume.name,
                value = { it.volume },
            ),
            BaseInfoRowSpec(
                dogmaName = TypeEntity::volume.name,
                value = { it.repackagedVolume },
                labelOverride = repackagedVolumeLabel,
                iconFileNameOverride = "type_3468_64.png",
            ),
            BaseInfoRowSpec(
                dogmaName = TypeEntity::capacity.name,
                value = { it.capacity },
            ),
            BaseInfoRowSpec(
                dogmaName = TypeEntity::mass.name,
                value = { it.mass },
            ),
        ).mapNotNull { spec ->
            val rawValue = spec.value(entity) ?: return@mapNotNull null
            val dogma = dogmaByName[spec.dogmaName]
            BaseDetailRowModel(
                iconFileName = spec.iconFileNameOverride ?: dogma?.iconFilename,
                label = spec.labelOverride ?: dogma?.displayName ?: spec.dogmaName,
                value = formatMappedValue(rawValue, dogma?.unitName),
            )
        }
    }

    if (detailItems.isEmpty()) return

    BaseContainer(
        title = stringResource(R.string.base_info),
        useSystemBarsPadding = false
    ) {
        Column {
            detailItems.forEachIndexed { index, itemModel ->
                BaseDetailRow(
                    model = itemModel,
                    showDivider = index != detailItems.lastIndex
                )
            }
        }
    }
}