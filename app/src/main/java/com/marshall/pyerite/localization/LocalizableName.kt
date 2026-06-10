package com.marshall.pyerite.localization

/**
 * SDE row or projection that exposes zh / en / canonical name columns for [displayName].
 */
interface LocalizableName {
    val zhName: String?
    val enName: String?
    val name: String?
}
