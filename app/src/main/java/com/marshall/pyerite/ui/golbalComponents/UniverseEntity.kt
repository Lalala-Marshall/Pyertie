package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.runtime.staticCompositionLocalOf

/** Character, corporation, or alliance shown in the entity-profile sheet. */
enum class UniverseEntityKind {
    CHARACTER,
    CORPORATION,
    ALLIANCE,
}

data class UniverseEntityRef(
    val kind: UniverseEntityKind,
    val id: Long,
)

/** Opens the entity-profile sheet. Hosted at the app NavHost; no-op if unset. */
val LocalOpenEntityProfile = staticCompositionLocalOf<(UniverseEntityRef) -> Unit> {
    {}
}
