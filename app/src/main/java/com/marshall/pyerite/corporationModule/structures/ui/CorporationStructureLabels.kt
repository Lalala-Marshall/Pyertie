package com.marshall.pyerite.corporationModule.structures.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.marshall.pyerite.R
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureFuelMonitor
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureServiceKind
import com.marshall.pyerite.corporationModule.structures.model.CorporationStructureState

@StringRes
internal fun CorporationStructureState.labelRes(): Int = when (this) {
    CorporationStructureState.SHIELD_VULNERABLE ->
        R.string.corporation_structures_state_shield_vulnerable
    CorporationStructureState.ARMOR_VULNERABLE ->
        R.string.corporation_structures_state_armor_vulnerable
    CorporationStructureState.HULL_VULNERABLE ->
        R.string.corporation_structures_state_hull_vulnerable
    CorporationStructureState.ARMOR_REINFORCE ->
        R.string.corporation_structures_state_armor_reinforce
    CorporationStructureState.HULL_REINFORCE ->
        R.string.corporation_structures_state_hull_reinforce
    CorporationStructureState.ANCHORING ->
        R.string.corporation_structures_state_anchoring
    CorporationStructureState.ANCHOR_VULNERABLE ->
        R.string.corporation_structures_state_anchor_vulnerable
    CorporationStructureState.DEPLOY_VULNERABLE ->
        R.string.corporation_structures_state_deploy_vulnerable
    CorporationStructureState.FITTING_INVULNERABLE ->
        R.string.corporation_structures_state_fitting_invulnerable
    CorporationStructureState.ONLINING_VULNERABLE ->
        R.string.corporation_structures_state_onlining_vulnerable
    CorporationStructureState.ONLINE_DEPRECATED ->
        R.string.corporation_structures_state_online
    CorporationStructureState.UNANCHORED ->
        R.string.corporation_structures_state_unanchored
    CorporationStructureState.UNKNOWN ->
        R.string.corporation_structures_state_unknown
}

@StringRes
internal fun CorporationStructureServiceKind.labelRes(): Int = when (this) {
    CorporationStructureServiceKind.MANUFACTURING_STANDARD ->
        R.string.corporation_structures_service_manufacturing_standard
    CorporationStructureServiceKind.MANUFACTURING_CAPITALS ->
        R.string.corporation_structures_service_manufacturing_capitals
    CorporationStructureServiceKind.MANUFACTURING_SUPERCAPITALS ->
        R.string.corporation_structures_service_manufacturing_supercapitals
    CorporationStructureServiceKind.RESEARCH ->
        R.string.corporation_structures_service_research
    CorporationStructureServiceKind.INVENTION ->
        R.string.corporation_structures_service_invention
    CorporationStructureServiceKind.MATERIAL_EFFICIENCY_RESEARCH ->
        R.string.corporation_structures_service_material_efficiency
    CorporationStructureServiceKind.TIME_EFFICIENCY_RESEARCH ->
        R.string.corporation_structures_service_time_efficiency
    CorporationStructureServiceKind.COPYING ->
        R.string.corporation_structures_service_copying
    CorporationStructureServiceKind.CLONE_BAY ->
        R.string.corporation_structures_service_clone_bay
    CorporationStructureServiceKind.MARKET ->
        R.string.corporation_structures_service_market
    CorporationStructureServiceKind.REPROCESSING ->
        R.string.corporation_structures_service_reprocessing
    CorporationStructureServiceKind.COMPOSITE_REACTIONS ->
        R.string.corporation_structures_service_composite_reactions
    CorporationStructureServiceKind.BIOCHEMICAL_REACTIONS ->
        R.string.corporation_structures_service_biochemical_reactions
    CorporationStructureServiceKind.HYBRID_REACTIONS ->
        R.string.corporation_structures_service_hybrid_reactions
    CorporationStructureServiceKind.MOON_DRILLING ->
        R.string.corporation_structures_service_moon_drilling
}

@StringRes
internal fun CorporationStructureFuelMonitor.labelRes(): Int = when (this) {
    CorporationStructureFuelMonitor.NONE -> R.string.corporation_structures_monitor_none
    CorporationStructureFuelMonitor.ONE_WEEK -> R.string.corporation_structures_monitor_one_week
    CorporationStructureFuelMonitor.TWO_WEEKS -> R.string.corporation_structures_monitor_two_weeks
    CorporationStructureFuelMonitor.THREE_WEEKS ->
        R.string.corporation_structures_monitor_three_weeks
    CorporationStructureFuelMonitor.ONE_MONTH -> R.string.corporation_structures_monitor_one_month
    CorporationStructureFuelMonitor.TWO_MONTHS ->
        R.string.corporation_structures_monitor_two_months
}

@Composable
internal fun structureStateColor(state: CorporationStructureState): Color {
    val res = when (state) {
        CorporationStructureState.ARMOR_REINFORCE -> R.color.corporation_structure_state_armor
        CorporationStructureState.HULL_REINFORCE -> R.color.corporation_structure_state_hull
        else -> R.color.corporation_structure_state_vulnerable
    }
    return colorResource(res)
}
