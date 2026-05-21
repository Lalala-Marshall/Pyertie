package com.marshall.pyerite.databaseHierarchyModule.util

import androidx.annotation.DrawableRes
import com.marshall.pyerite.R

/** EVE certificate level badges (0–5), from Eve University wiki assets. */
@DrawableRes
fun certificateLevelDrawable(level: Int): Int = when (level.coerceIn(0, 5)) {
    0 -> R.drawable.certificate_level0
    1 -> R.drawable.certificate_level1
    2 -> R.drawable.certificate_level2
    3 -> R.drawable.certificate_level3
    4 -> R.drawable.certificate_level4
    else -> R.drawable.certificate_level5
}
