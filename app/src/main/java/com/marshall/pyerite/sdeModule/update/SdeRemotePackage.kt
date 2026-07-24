package com.marshall.pyerite.sdeModule.update

data class SdeRemotePackage(
    val meta: SdeReleaseMeta,
    val assetUrls: Map<String, String>,
) {

    fun urlFor(assetName: String): String? = assetUrls[assetName]
}
