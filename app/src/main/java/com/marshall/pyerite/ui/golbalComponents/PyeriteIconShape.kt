package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Shared rounded-square clip for list / detail leading icons and portraits.
 * Percent of the shorter side keeps the same look at any size.
 */
object PyeriteIconShape {
    const val CORNER_PERCENT = 17
    val shape = RoundedCornerShape(percent = CORNER_PERCENT)
}
