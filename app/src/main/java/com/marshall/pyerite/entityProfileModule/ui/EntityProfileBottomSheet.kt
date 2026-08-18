package com.marshall.pyerite.entityProfileModule.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.marshall.pyerite.R
import com.marshall.pyerite.entityProfileModule.model.EntityEmploymentEntry
import com.marshall.pyerite.entityProfileModule.model.EntityProfile
import com.marshall.pyerite.entityProfileModule.model.EntityProfileConfig
import com.marshall.pyerite.entityProfileModule.model.EntityStandingRow
import com.marshall.pyerite.entityProfileModule.model.EntityStandingSection
import com.marshall.pyerite.entityProfileModule.model.EntityStandingSectionKind
import com.marshall.pyerite.entityProfileModule.viewModel.EntityProfileViewModel
import com.marshall.pyerite.eveAuthModule.sso.EveSsoConfig
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItem
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemHint
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
import com.marshall.pyerite.ui.golbalComponents.CharacterAvatar
import com.marshall.pyerite.ui.golbalComponents.PyeriteIconShape
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityKind
import com.marshall.pyerite.ui.golbalComponents.UniverseEntityRef
import com.marshall.pyerite.util.DurationDisplayFormatter
import com.marshall.pyerite.util.formatDurationDisplay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

private enum class EntityProfileTab {
    STANDINGS,
    EMPLOYMENT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntityProfileBottomSheet(
    viewModel: EntityProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val page = uiState.current ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetCorner = dimensionResource(R.dimen.character_mail_compose_sheet_corner)
    val sheetBackground = colorResource(R.color.search_field_idle_background)
    val sheetHorizontalPadding =
        dimensionResource(R.dimen.character_mail_compose_sheet_horizontal_padding)

    ModalBottomSheet(
        onDismissRequest = viewModel::dismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = sheetCorner, topEnd = sheetCorner),
        containerColor = sheetBackground,
        scrimColor = colorResource(R.color.search_scrim),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(EntityProfileConfig.HEIGHT_FRACTION)
                .background(sheetBackground)
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = sheetHorizontalPadding,
                    end = sheetHorizontalPadding,
                    top = dimensionResource(R.dimen.character_mail_compose_header_top_padding),
                ),
            ) {
                EntityProfileSheetHeader(
                    canPop = uiState.canPop,
                    onBack = viewModel::pop,
                    onDone = viewModel::dismiss,
                )
            }
            EntityProfileSheetBody(
                pageRef = page.ref,
                profile = page.profile,
                isLoading = page.isLoading,
                loadFailed = page.loadFailed,
                onRetry = viewModel::retry,
                onOpenEntity = viewModel::openChild,
            )
        }
    }
}

@Composable
private fun EntityProfileSheetHeader(
    canPop: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val buttonHeight = dimensionResource(R.dimen.top_bar_back_button_size)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
    ) {
        if (canPop) {
            EntityProfileHeaderAction(
                label = stringResource(R.string.entity_profile_back),
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
        EntityProfileDonePill(
            onClick = onDone,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun EntityProfileHeaderAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        color = colorResource(R.color.hyperlink_text),
        fontSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
    )
}

@Composable
private fun EntityProfileDonePill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.entity_profile_done),
        color = colorResource(R.color.text_primary),
        fontSize = dimensionResource(R.dimen.sub_menu_label_text_size).value.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.entity_profile_done_pill_corner)))
            .background(colorResource(R.color.skill_plan_dialog_button_background))
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.entity_profile_done_pill_horizontal_padding),
                vertical = dimensionResource(R.dimen.entity_profile_done_pill_vertical_padding),
            )
            .semantics { role = Role.Button },
    )
}

@Composable
private fun ColumnScope.EntityProfileSheetBody(
    pageRef: UniverseEntityRef,
    profile: EntityProfile?,
    isLoading: Boolean,
    loadFailed: Boolean,
    onRetry: () -> Unit,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    val sectionGap = dimensionResource(R.dimen.type_detail_section_gap)
    val bottomPadding = dimensionResource(R.dimen.type_detail_bottom_padding)
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(bottom = bottomPadding),
    ) {
        if (loadFailed && profile == null) {
            EntityProfileLoadFailed(onRetry = onRetry)
            return
        }
        if (profile == null && isLoading) {
            Text(
                text = stringResource(R.string.character_sheet_loading),
                color = colorResource(R.color.hint_text),
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding),
                    vertical = sectionGap,
                ),
            )
            return
        }
        val loaded = profile ?: return
        Spacer(modifier = Modifier.height(sectionGap))
        EntityProfileHeaderCard(
            profile = loaded,
            onOpenEntity = onOpenEntity,
        )
        Spacer(modifier = Modifier.height(sectionGap))
        EntityProfileLinksCard(profile = loaded)
        val description = loaded.descriptionHtml
        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(sectionGap))
            EntityProfileDescriptionCard(html = description)
        }
        Spacer(modifier = Modifier.height(sectionGap))
        EntityProfileDetailsCard(
            pageRef = pageRef,
            profile = loaded,
            onOpenEntity = onOpenEntity,
        )
    }
}

@Composable
private fun EntityProfileLoadFailed(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.detail_card_horizontal_padding),
                vertical = dimensionResource(R.dimen.type_detail_section_gap),
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.entity_profile_load_failed),
            color = colorResource(R.color.text_primary),
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.entity_profile_retry))
        }
    }
}

@Composable
private fun EntityProfileHeaderCard(
    profile: EntityProfile,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    val context = LocalContext.current
    val copySuccess = stringResource(R.string.entity_profile_copy_id_success)
    val copyLabel = stringResource(R.string.entity_profile_copy_id)
    val nonePlaceholder = stringResource(R.string.character_org_none_placeholder)
    val avatarSize = dimensionResource(R.dimen.entity_profile_header_avatar_size)
    val corpClick = profile.corporationId
        ?.takeIf { profile.ref.kind != UniverseEntityKind.CORPORATION }
        ?.let { id ->
            { onOpenEntity(UniverseEntityRef(UniverseEntityKind.CORPORATION, id)) }
        }
    val allianceClick = profile.allianceId
        ?.takeIf { profile.ref.kind != UniverseEntityKind.ALLIANCE }
        ?.let { id ->
            { onOpenEntity(UniverseEntityRef(UniverseEntityKind.ALLIANCE, id)) }
        }

    BaseContainer(title = null, useSystemBarsPadding = false) {
        Column {
            BaseLazyColumnItem(
                model = BaseLazyColumnItemModel(
                    itemName = profile.name,
                    itemNameBold = true,
                    itemHints = buildList {
                        if (profile.isCeo) {
                            add(
                                BaseLazyColumnItemHint(
                                    text = stringResource(R.string.entity_profile_ceo),
                                    color = colorResource(R.color.text_primary),
                                ),
                            )
                        }
                        if (profile.ref.kind != UniverseEntityKind.CORPORATION) {
                            add(
                                BaseLazyColumnItemHint(
                                    text = profile.corporationName?.takeIf { it.isNotBlank() }
                                        ?: nonePlaceholder,
                                    color = colorResource(
                                        if (corpClick != null) {
                                            R.color.hyperlink_text
                                        } else {
                                            R.color.text_primary
                                        },
                                    ),
                                    iconUrl = profile.corporationIconUrl,
                                    onClick = corpClick,
                                ),
                            )
                        }
                        if (profile.ref.kind != UniverseEntityKind.ALLIANCE &&
                            (profile.allianceId != null || !profile.allianceName.isNullOrBlank())
                        ) {
                            add(
                                BaseLazyColumnItemHint(
                                    text = profile.allianceName?.takeIf { it.isNotBlank() }
                                        ?: nonePlaceholder,
                                    color = colorResource(
                                        if (allianceClick != null) {
                                            R.color.hyperlink_text
                                        } else {
                                            R.color.text_primary
                                        },
                                    ),
                                    iconUrl = profile.allianceIconUrl,
                                    onClick = allianceClick,
                                ),
                            )
                        }
                    },
                    showChevron = false,
                    onClick = null,
                ),
                showDivider = false,
                leadingContent = { _ ->
                    CharacterAvatar(
                        portraitUrl = profile.iconUrl,
                        size = avatarSize,
                        shape = PyeriteIconShape.shape,
                    )
                },
                titleTrailingContent = if (profile.isNpcCorporation &&
                    profile.ref.kind == UniverseEntityKind.CORPORATION
                ) {
                    { EntityProfileNpcTag() }
                } else {
                    null
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(R.dimen.detail_row_horizontal_padding),
                        end = dimensionResource(R.dimen.detail_row_horizontal_padding),
                        bottom = dimensionResource(R.dimen.entity_profile_header_id_top_gap),
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Row(
                    modifier = Modifier.clickable {
                        copyEntityId(context, profile.ref.id, copyLabel, copySuccess)
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.entity_profile_id, profile.ref.id),
                        color = colorResource(R.color.hyperlink_text),
                        fontSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp,
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.entity_profile_copy_icon_gap)))
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = copyLabel,
                        tint = colorResource(R.color.hyperlink_text),
                        modifier = Modifier.size(dimensionResource(R.dimen.entity_profile_copy_icon_size)),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntityProfileLinksCard(profile: EntityProfile) {
    val context = LocalContext.current
    BaseContainer(title = null, useSystemBarsPadding = false) {
        Column {
            if (profile.factionName != null) {
                BaseLazyColumnItem(
                    model = BaseLazyColumnItemModel(
                        iconUrl = profile.factionIconUrl,
                        itemName = stringResource(R.string.entity_profile_faction),
                        itemHints = listOf(
                            BaseLazyColumnItemHint(
                                text = profile.factionName,
                                color = colorResource(R.color.text_primary),
                            ),
                        ),
                        showChevron = false,
                        onClick = null,
                    ),
                    showDivider = true,
                )
            }
            EntityProfileExternalLinkRow(
                label = stringResource(R.string.entity_profile_evewho),
                url = EntityProfileConfig.eveWhoUrl(profile.ref.kind, profile.ref.id),
                showDivider = true,
                onOpen = { openExternalUrl(context, it) },
            )
            EntityProfileExternalLinkRow(
                label = stringResource(R.string.entity_profile_zkillboard),
                url = EntityProfileConfig.zKillUrl(profile.ref.kind, profile.ref.id),
                showDivider = false,
                onOpen = { openExternalUrl(context, it) },
            )
        }
    }
}

@Composable
private fun EntityProfileExternalLinkRow(
    label: String,
    url: String,
    showDivider: Boolean,
    onOpen: (String) -> Unit,
) {
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            showLeadingIcon = false,
            itemName = label,
            itemNameColor = colorResource(R.color.hyperlink_text),
            showChevron = false,
            onClick = null,
            itemNameOnClick = { onOpen(url) },
        ),
        showDivider = showDivider,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = stringResource(R.string.entity_profile_open_external),
                tint = colorResource(R.color.hyperlink_text),
                modifier = Modifier
                    .size(dimensionResource(R.dimen.entity_profile_link_icon_size))
                    .clickable { onOpen(url) },
            )
        },
    )
}

@Composable
private fun EntityProfileDescriptionCard(html: String) {
    val textColor = colorResource(R.color.text_primary).toArgb()
    val textSizeSp = dimensionResource(R.dimen.type_detail_body_text_size).value
    val rendered = remember(html) { entityProfileHtmlToCharSequence(html) }
    BaseContainer(
        title = stringResource(R.string.entity_profile_description),
        useSystemBarsPadding = false,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.entity_profile_description_padding)),
            factory = { context ->
                TextView(context).apply {
                    setTextColor(textColor)
                    textSize = textSizeSp
                    setTextIsSelectable(true)
                    linksClickable = false
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                }
            },
            update = { view ->
                view.setTextColor(textColor)
                view.text = rendered
            },
        )
    }
}

@Composable
private fun EntityProfileDetailsCard(
    pageRef: UniverseEntityRef,
    profile: EntityProfile,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    val showEmployment = profile.ref.kind == UniverseEntityKind.CHARACTER
    var selectedTab by remember(pageRef) {
        mutableStateOf(EntityProfileTab.STANDINGS)
    }
    BaseContainer(title = null, useSystemBarsPadding = false) {
        Column {
            if (showEmployment) {
                EntityProfileTabRow(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                )
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.detail_divider_thickness),
                    color = colorResource(R.color.border),
                )
            }
            when {
                selectedTab == EntityProfileTab.EMPLOYMENT && showEmployment -> {
                    EntityProfileEmploymentList(
                        entries = profile.employment,
                        detailsReady = profile.detailsReady,
                        onOpenEntity = onOpenEntity,
                    )
                }
                else -> {
                    EntityProfileStandingsList(
                        sections = profile.standingSections,
                        detailsReady = profile.detailsReady,
                    )
                }
            }
        }
    }
}

@Composable
private fun EntityProfileTabRow(
    selected: EntityProfileTab,
    onSelect: (EntityProfileTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.entity_profile_section_title_padding)),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.entity_profile_tab_row_gap),
        ),
    ) {
        EntityProfileTabChip(
            label = stringResource(R.string.entity_profile_tab_standings),
            selected = selected == EntityProfileTab.STANDINGS,
            onClick = { onSelect(EntityProfileTab.STANDINGS) },
        )
        EntityProfileTabChip(
            label = stringResource(R.string.entity_profile_tab_employment),
            selected = selected == EntityProfileTab.EMPLOYMENT,
            onClick = { onSelect(EntityProfileTab.EMPLOYMENT) },
        )
    }
}

@Composable
private fun EntityProfileTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) {
        colorResource(R.color.entity_profile_tab_selected_background)
    } else {
        colorResource(R.color.search_field_idle_background)
    }
    val textColor = if (selected) {
        colorResource(R.color.entity_profile_tab_selected_text)
    } else {
        colorResource(R.color.text_primary)
    }
    Text(
        text = label,
        color = textColor,
        fontSize = dimensionResource(R.dimen.entity_profile_tab_text_size).value.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.entity_profile_tab_corner)))
            .background(background)
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.entity_profile_tab_horizontal_padding),
                vertical = dimensionResource(R.dimen.entity_profile_tab_vertical_padding),
            ),
    )
}

@Composable
private fun EntityProfileStandingsList(
    sections: List<EntityStandingSection>,
    detailsReady: Boolean,
) {
    if (!detailsReady && sections.isEmpty()) {
        EntityProfilePendingPlaceholder()
        return
    }
    Column {
        sections.forEachIndexed { index, section ->
            Text(
                text = standingSectionTitle(section.kind),
                color = colorResource(R.color.hint_text),
                fontSize = dimensionResource(R.dimen.entity_profile_section_title_text_size).value.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.entity_profile_section_title_padding),
                    end = dimensionResource(R.dimen.entity_profile_section_title_padding),
                    top = dimensionResource(R.dimen.entity_profile_section_title_padding),
                    bottom = dimensionResource(R.dimen.detail_row_label_subtitle_spacing),
                ),
            )
            section.rows.forEach { row ->
                EntityProfileStandingRow(row = row)
            }
            if (index != sections.lastIndex) {
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.detail_divider_thickness),
                    color = colorResource(R.color.border),
                )
            }
        }
    }
}

@Composable
private fun standingSectionTitle(kind: EntityStandingSectionKind): String = when (kind) {
    EntityStandingSectionKind.PERSONAL -> stringResource(R.string.entity_profile_standings_personal)
    EntityStandingSectionKind.CORPORATION ->
        stringResource(R.string.entity_profile_standings_corporation)
    EntityStandingSectionKind.ALLIANCE -> stringResource(R.string.entity_profile_standings_alliance)
}

@Composable
private fun EntityProfileStandingRow(row: EntityStandingRow) {
    val avatarSize = dimensionResource(R.dimen.entity_profile_standing_avatar_size)
    val nameGap = dimensionResource(R.dimen.entity_profile_standing_name_gap)
    val valuePadding = dimensionResource(R.dimen.entity_profile_standing_value_horizontal_padding)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.detail_row_horizontal_padding),
                vertical = dimensionResource(R.dimen.entity_profile_standing_row_vertical_padding),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EntityProfileStandingParty(
            name = row.from.name,
            iconUrl = row.from.iconUrl,
            avatarSize = avatarSize,
            nameGap = nameGap,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = EntityProfileConfig.formatStanding(row.standing),
            color = standingValueColor(row.standing),
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensionResource(R.dimen.sub_menu_value_text_size).value.sp,
            modifier = Modifier.padding(horizontal = valuePadding),
        )
        EntityProfileStandingParty(
            name = row.toward.name,
            iconUrl = row.toward.iconUrl,
            avatarSize = avatarSize,
            nameGap = nameGap,
            modifier = Modifier.weight(1f),
            reverse = true,
        )
    }
}

@Composable
private fun EntityProfileStandingParty(
    name: String,
    iconUrl: String?,
    avatarSize: Dp,
    nameGap: Dp,
    modifier: Modifier = Modifier,
    reverse: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (reverse) Arrangement.End else Arrangement.Start,
    ) {
        if (!reverse) {
            CharacterAvatar(
                portraitUrl = iconUrl,
                size = avatarSize,
                shape = PyeriteIconShape.shape,
            )
            Spacer(modifier = Modifier.width(nameGap))
        }
        Text(
            text = name,
            color = colorResource(R.color.text_primary),
            fontSize = dimensionResource(R.dimen.detail_row_label_subtitle_text_size).value.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (reverse) {
            Spacer(modifier = Modifier.width(nameGap))
            CharacterAvatar(
                portraitUrl = iconUrl,
                size = avatarSize,
                shape = PyeriteIconShape.shape,
            )
        }
    }
}

@Composable
private fun standingValueColor(value: Double): Color = when {
    abs(value) < EntityProfileConfig.STANDING_ZERO_EPSILON ->
        colorResource(R.color.text_primary)
    value > 0.0 -> colorResource(R.color.character_status_positive)
    else -> colorResource(R.color.character_security_negative)
}

@Composable
private fun EntityProfileEmploymentList(
    entries: List<EntityEmploymentEntry>,
    detailsReady: Boolean,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    if (!detailsReady && entries.isEmpty()) {
        EntityProfilePendingPlaceholder()
        return
    }
    Column {
        entries.forEachIndexed { index, entry ->
            EntityProfileEmploymentRow(
                entry = entry,
                showDivider = index != entries.lastIndex,
                onOpenEntity = onOpenEntity,
            )
        }
    }
}

@Composable
private fun EntityProfileEmploymentRow(
    entry: EntityEmploymentEntry,
    showDivider: Boolean,
    onOpenEntity: (UniverseEntityRef) -> Unit,
) {
    val present = stringResource(R.string.entity_profile_employment_present)
    val placeholder = stringResource(R.string.character_sheet_value_placeholder)
    val startText = formatEntityProfileDate(entry.startEpochMs)
    val endText = entry.endEpochMs?.let(::formatEntityProfileDate) ?: present
    val endMs = entry.endEpochMs ?: System.currentTimeMillis()
    val durationSeconds = ((endMs - entry.startEpochMs).coerceAtLeast(0L)) /
        EveSsoConfig.MILLIS_PER_SECOND
    val durationText = formatDurationDisplay(
        totalSeconds = durationSeconds,
        includeSeconds = false,
        maxUnit = DurationDisplayFormatter.MaxUnit.DAY,
    )
    val rangeText = stringResource(
        R.string.entity_profile_employment_range,
        startText,
        endText,
        durationText,
    )
    val corpName = entry.corporationName.ifBlank { placeholder }
    val allianceClick = entry.allianceId?.let { id ->
        { onOpenEntity(UniverseEntityRef(UniverseEntityKind.ALLIANCE, id)) }
    }
    BaseLazyColumnItem(
        model = BaseLazyColumnItemModel(
            iconUrl = entry.corporationIconUrl,
            itemName = corpName,
            itemNameColor = colorResource(R.color.hyperlink_text),
            itemHints = buildList {
                entry.allianceName?.takeIf { it.isNotBlank() }?.let { allianceName ->
                    add(
                        BaseLazyColumnItemHint(
                            text = allianceName,
                            color = colorResource(
                                if (allianceClick != null) {
                                    R.color.hyperlink_text
                                } else {
                                    R.color.text_primary
                                },
                            ),
                            iconUrl = entry.allianceIconUrl,
                            onClick = allianceClick,
                        ),
                    )
                }
                add(BaseLazyColumnItemHint(text = rangeText))
            },
            showChevron = false,
            onClick = null,
            itemNameOnClick = {
                onOpenEntity(
                    UniverseEntityRef(UniverseEntityKind.CORPORATION, entry.corporationId),
                )
            },
        ),
        showDivider = showDivider,
        titleTrailingContent = if (entry.isNpc) {
            { EntityProfileNpcTag() }
        } else {
            null
        },
    )
}

@Composable
private fun EntityProfileNpcTag() {
    Text(
        text = stringResource(R.string.entity_profile_npc_tag),
        color = colorResource(R.color.white),
        fontSize = dimensionResource(R.dimen.entity_profile_npc_tag_text_size).value.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.entity_profile_npc_tag_corner)))
            .background(colorResource(R.color.entity_profile_npc_tag))
            .padding(
                horizontal = dimensionResource(R.dimen.entity_profile_npc_tag_horizontal_padding),
                vertical = dimensionResource(R.dimen.entity_profile_npc_tag_vertical_padding),
            ),
    )
}

@Composable
private fun EntityProfilePendingPlaceholder() {
    Text(
        text = stringResource(R.string.character_sheet_loading),
        color = colorResource(R.color.hint_text),
        modifier = Modifier.padding(dimensionResource(R.dimen.entity_profile_section_title_padding)),
    )
}

private fun formatEntityProfileDate(epochMs: Long): String {
    return SimpleDateFormat(EntityProfileConfig.DISPLAY_DATE_PATTERN, Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }.format(Date(epochMs))
}

private fun copyEntityId(
    context: Context,
    id: Long,
    label: String,
    successMessage: String,
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, id.toString()))
    Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
}

private fun openExternalUrl(context: Context, url: String) {
    val uri = url.toUri()
    runCatching {
        CustomTabsIntent.Builder().build().launchUrl(context, uri)
    }.onFailure {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
