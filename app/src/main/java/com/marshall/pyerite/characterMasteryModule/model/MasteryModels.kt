package com.marshall.pyerite.characterMasteryModule.model

import com.marshall.pyerite.localization.LocalizableName

/** Toolbar / group-route mastery filter. */
enum class MasteryFilter {
    ALL,
    LOCKED,
    QUALIFIED,
    LEVEL_0,
    LEVEL_1,
    LEVEL_2,
    LEVEL_3,
    LEVEL_4,
    LEVEL_5,
    ;

    val masteryLevel: Int?
        get() = when (this) {
            LEVEL_0 -> 0
            LEVEL_1 -> 1
            LEVEL_2 -> 2
            LEVEL_3 -> 3
            LEVEL_4 -> 4
            LEVEL_5 -> 5
            else -> null
        }

    fun matches(state: MasteryLevelState): Boolean = when (this) {
        ALL -> true
        LOCKED -> state == MasteryLevelState.Locked
        QUALIFIED -> state is MasteryLevelState.Level
        LEVEL_0 -> state == MasteryLevelState.Level(0)
        LEVEL_1 -> state == MasteryLevelState.Level(1)
        LEVEL_2 -> state == MasteryLevelState.Level(2)
        LEVEL_3 -> state == MasteryLevelState.Level(3)
        LEVEL_4 -> state == MasteryLevelState.Level(4)
        LEVEL_5 -> state == MasteryLevelState.Level(5)
    }

    /** Status filters (all / locked / qualified) vs mastery-level filters. */
    val showMenuDividerBelow: Boolean
        get() = this == QUALIFIED
}

sealed class MasteryLevelState {
    data object Locked : MasteryLevelState()
    data class Level(val level: Int) : MasteryLevelState()
}

data class MasteryShip(
    val typeId: Int,
    val groupId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
    val metaGroupId: Int?,
    val published: Boolean,
    val state: MasteryLevelState,
) : LocalizableName

data class MasteryGroup(
    val groupId: Int,
    override val name: String?,
    override val zhName: String?,
    override val enName: String?,
    val iconFilename: String?,
    val ships: List<MasteryShip>,
) : LocalizableName {
    fun matchingCount(filter: MasteryFilter): Int =
        ships.count { filter.matches(it.state) }
}

data class MasteryMetaGroup(
    val id: Int,
    val name: String?,
)

data class CharacterMasterySnapshot(
    val groups: List<MasteryGroup>,
    val metaGroups: List<MasteryMetaGroup>,
)
