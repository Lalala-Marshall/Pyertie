package com.marshall.pyerite.databaseHierarchyModule.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types")
data class TypeEntity(
    @PrimaryKey
    @ColumnInfo(name = "type_id")
    val id: Int,
    @ColumnInfo(name = "groupID") val groupId: Int? = null,
    val name: String? = null,
    @ColumnInfo(name = "de_name") val deName: String? = null,
    @ColumnInfo(name = "en_name") val enName: String? = null,
    @ColumnInfo(name = "es_name") val esName: String? = null,
    @ColumnInfo(name = "fr_name") val frName: String? = null,
    @ColumnInfo(name = "ja_name") val jaName: String? = null,
    @ColumnInfo(name = "ko_name") val koName: String? = null,
    @ColumnInfo(name = "ru_name") val ruName: String? = null,
    @ColumnInfo(name = "zh_name") val zhName: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "icon_filename") val iconFilename: String? = null,
    @ColumnInfo(name = "bpc_icon_filename") val bpcIconFilename: String? = null,
    val published: Boolean? = null,
    val volume: Double? = null,
    @ColumnInfo(name = "repackaged_volume") val repackagedVolume: Double? = null,
    val capacity: Double? = null,
    val mass: Double? = null,
    val marketGroupID: Int? = null,
    val metaGroupID: Int? = null,
    val iconID: Int? = null,
    @ColumnInfo(name = "group_name") val groupName: String? = null,
    @ColumnInfo(name = "categoryID") val categoryID: Int? = null,
    @ColumnInfo(name = "category_name") val categoryName: String? = null,
    @ColumnInfo(name = "pg_need") val pgNeed: Double? = null,
    @ColumnInfo(name = "cpu_need") val cpuNeed: Double? = null,
    @ColumnInfo(name = "rig_cost") val rigCost: Int? = null,
    @ColumnInfo(name = "em_damage") val emDamage: Double? = null,
    @ColumnInfo(name = "them_damage") val themDamage: Double? = null,
    @ColumnInfo(name = "kin_damage") val kinDamage: Double? = null,
    @ColumnInfo(name = "exp_damage") val expDamage: Double? = null,
    @ColumnInfo(name = "high_slot") val highSlot: Int? = null,
    @ColumnInfo(name = "mid_slot") val midSlot: Int? = null,
    @ColumnInfo(name = "low_slot") val lowSlot: Int? = null,
    @ColumnInfo(name = "rig_slot") val rigSlot: Int? = null,
    @ColumnInfo(name = "gun_slot") val gunSlot: Int? = null,
    @ColumnInfo(name = "miss_slot") val missSlot: Int? = null,
    @ColumnInfo(name = "variationParentTypeID") val variationParentTypeID: Int? = null,
    @ColumnInfo(name = "process_size") val processSize: Int? = null,
    @ColumnInfo(name = "npc_ship_scene") val npcShipScene: String? = null,
    @ColumnInfo(name = "npc_ship_faction") val npcShipFaction: String? = null,
    @ColumnInfo(name = "npc_ship_type") val npcShipType: String? = null,
    @ColumnInfo(name = "npc_ship_faction_icon") val npcShipFactionIcon: String? = null
)
