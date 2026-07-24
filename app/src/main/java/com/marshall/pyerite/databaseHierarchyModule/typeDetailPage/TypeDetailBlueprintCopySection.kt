package com.marshall.pyerite.databaseHierarchyModule.typeDetailPage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marshall.pyerite.R
import com.marshall.pyerite.sdeModule.room.industry.BlueprintCopyDetail
import com.marshall.pyerite.databaseHierarchyModule.util.formatDurationFromSeconds
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuValueRow
import com.marshall.pyerite.ui.golbalComponents.BaseSubMenuValueRowModel

@Composable
fun TypeDetailBlueprintCopySection(
    copyDetail: BlueprintCopyDetail?,
) {
    val formattedTime = formatDurationFromSeconds(copyDetail?.copyingTimeSeconds)
    val maxRunsPerCopy = copyDetail?.maxRunsPerCopy
    val formattedMaxRuns = maxRunsPerCopy?.takeIf { it > 0 }?.let(::formatIndustryCount).orEmpty()
    if (formattedTime.isEmpty() && formattedMaxRuns.isEmpty()) return

    val showTime = formattedTime.isNotEmpty()
    val showMaxRuns = formattedMaxRuns.isNotEmpty()

    BaseContainer(
        title = stringResource(R.string.type_detail_section_copy),
        useSystemBarsPadding = false,
    ) {
        Column {
            if (showTime) {
                BaseSubMenuValueRow(
                    model = BaseSubMenuValueRowModel(
                        label = stringResource(R.string.type_detail_copy_time),
                        value = formattedTime,
                    ),
                    showDivider = showMaxRuns,
                )
            }
            if (showMaxRuns) {
                BaseSubMenuValueRow(
                    model = BaseSubMenuValueRowModel(
                        label = stringResource(R.string.type_detail_copy_max_runs),
                        value = formattedMaxRuns,
                    ),
                    showDivider = false,
                )
            }
        }
    }
}
