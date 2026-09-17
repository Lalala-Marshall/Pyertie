package com.marshall.pyerite.peoplePlacesModule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesBuildingFilters
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesBuildingType
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCategory
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCharacterFilters
import com.marshall.pyerite.peoplePlacesModule.model.PeoplePlacesCorporationFilters
import com.marshall.pyerite.peoplePlacesModule.viewModel.PeoplePlacesUiState
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel

@Composable
internal fun PeoplePlacesFilterSection(
    uiState: PeoplePlacesUiState,
    onCharacterFiltersChange: (PeoplePlacesCharacterFilters, Boolean) -> Unit,
    onCorporationFiltersChange: (PeoplePlacesCorporationFilters, Boolean) -> Unit,
    onAllianceExactMatchChange: (Boolean) -> Unit,
    onBuildingFiltersChange: (PeoplePlacesBuildingFilters, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    BaseContainer(
        title = stringResource(R.string.people_places_filters_title),
        useSystemBarsPadding = false,
    ) {
        when (uiState.category) {
            PeoplePlacesCategory.CHARACTER -> CharacterFilters(
                filters = uiState.characterFilters,
                showMyCorporation = uiState.showMyCorporationFilter,
                showMyAlliance = uiState.showMyAllianceFilter,
                onChange = onCharacterFiltersChange,
                onClearFilters = onClearFilters,
            )
            PeoplePlacesCategory.CORPORATION -> CorporationFilters(
                filters = uiState.corporationFilters,
                showMyAlliance = uiState.showMyAllianceFilter,
                onChange = onCorporationFiltersChange,
                onClearFilters = onClearFilters,
            )
            PeoplePlacesCategory.ALLIANCE -> {
                PeoplePlacesSwitchRow(
                    label = stringResource(R.string.people_places_exact_match),
                    checked = uiState.allianceExactMatch,
                    onCheckedChange = onAllianceExactMatchChange,
                    showDivider = false,
                )
            }
            PeoplePlacesCategory.STRUCTURE -> BuildingFilters(
                filters = uiState.buildingFilters,
                onChange = onBuildingFiltersChange,
                onClearFilters = onClearFilters,
            )
        }
    }
}

@Composable
private fun CharacterFilters(
    filters: PeoplePlacesCharacterFilters,
    showMyCorporation: Boolean,
    showMyAlliance: Boolean,
    onChange: (PeoplePlacesCharacterFilters, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    PeoplePlacesFilterTextField(
        value = filters.corporationQuery,
        hint = stringResource(R.string.people_places_hint_corporation),
        onValueChange = { onChange(filters.copy(corporationQuery = it), false) },
    )
    PeoplePlacesFilterTextField(
        value = filters.allianceQuery,
        hint = stringResource(R.string.people_places_hint_alliance),
        onValueChange = { onChange(filters.copy(allianceQuery = it), false) },
    )
    if (showMyCorporation) {
        PeoplePlacesSwitchRow(
            label = stringResource(R.string.people_places_my_corporation),
            checked = filters.myCorporationOnly,
            onCheckedChange = { onChange(filters.copy(myCorporationOnly = it), true) },
            showDivider = true,
        )
    }
    if (showMyAlliance) {
        PeoplePlacesSwitchRow(
            label = stringResource(R.string.people_places_my_alliance),
            checked = filters.myAllianceOnly,
            onCheckedChange = { onChange(filters.copy(myAllianceOnly = it), true) },
            showDivider = true,
        )
    }
    PeoplePlacesSwitchRow(
        label = stringResource(R.string.people_places_exact_match),
        checked = filters.exactMatch,
        onCheckedChange = { onChange(filters.copy(exactMatch = it), true) },
        showDivider = true,
    )
    PeoplePlacesClearFiltersRow(onClick = onClearFilters)
}

@Composable
private fun CorporationFilters(
    filters: PeoplePlacesCorporationFilters,
    showMyAlliance: Boolean,
    onChange: (PeoplePlacesCorporationFilters, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    PeoplePlacesFilterTextField(
        value = filters.allianceQuery,
        hint = stringResource(R.string.people_places_hint_alliance),
        onValueChange = { onChange(filters.copy(allianceQuery = it), false) },
    )
    if (showMyAlliance) {
        PeoplePlacesSwitchRow(
            label = stringResource(R.string.people_places_my_alliance),
            checked = filters.myAllianceOnly,
            onCheckedChange = { onChange(filters.copy(myAllianceOnly = it), true) },
            showDivider = true,
        )
    }
    PeoplePlacesSwitchRow(
        label = stringResource(R.string.people_places_exact_match),
        checked = filters.exactMatch,
        onCheckedChange = { onChange(filters.copy(exactMatch = it), true) },
        showDivider = true,
    )
    PeoplePlacesClearFiltersRow(onClick = onClearFilters)
}

@Composable
private fun BuildingFilters(
    filters: PeoplePlacesBuildingFilters,
    onChange: (PeoplePlacesBuildingFilters, Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    PeoplePlacesBuildingTypeRow(
        selected = filters.buildingType,
        onSelect = { onChange(filters.copy(buildingType = it), true) },
    )
    PeoplePlacesSwitchRow(
        label = stringResource(R.string.people_places_exact_match),
        checked = filters.exactMatch,
        onCheckedChange = { onChange(filters.copy(exactMatch = it), true) },
        showDivider = true,
    )
    PeoplePlacesClearFiltersRow(onClick = onClearFilters)
}

@Composable
private fun PeoplePlacesBuildingTypeRow(
    selected: PeoplePlacesBuildingType,
    onSelect: (PeoplePlacesBuildingType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        BaseLazyColumnItem(
            model = BaseLazyColumnItemModel(
                showLeadingIcon = false,
                itemName = stringResource(selected.labelRes()),
                showChevron = true,
                chevronExpanded = expanded,
                onClick = { expanded = true },
            ),
            showDivider = true,
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PeoplePlacesBuildingType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(type.labelRes())) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PeoplePlacesSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = label,
            showChevron = false,
            onClick = { onCheckedChange(!checked) },
        ),
        showDivider = showDivider,
        trailingContent = {
            val checkedTrack = colorResource(R.color.character_status_positive)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = checkedTrack,
                    checkedBorderColor = checkedTrack,
                    checkedThumbColor = colorResource(R.color.white),
                ),
            )
        },
    )
}

@Composable
private fun PeoplePlacesClearFiltersRow(onClick: () -> Unit) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = stringResource(R.string.people_places_clear_filters),
            itemNameColor = colorResource(R.color.character_delete),
            showChevron = false,
            onClick = onClick,
        ),
        showDivider = false,
    )
}

@Composable
private fun PeoplePlacesFilterTextField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textColor = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)
    val titleTextSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp
    val titleLineHeight = dimensionResource(R.dimen.sub_menu_label_line_height).value.sp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.detail_row_horizontal_padding),
                vertical = dimensionResource(R.dimen.detail_row_vertical_padding_single_line),
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = textColor,
                fontSize = titleTextSize,
                lineHeight = titleLineHeight,
            ),
            singleLine = true,
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            color = hintColor,
                            fontSize = titleTextSize,
                            lineHeight = titleLineHeight,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.detail_row_horizontal_padding),
        ),
        thickness = dimensionResource(R.dimen.detail_divider_thickness),
        color = colorResource(R.color.border),
    )
}
