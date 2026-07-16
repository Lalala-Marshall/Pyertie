package com.marshall.pyerite.data.sde

import com.marshall.pyerite.data.network.PyeriteJson
import com.marshall.pyerite.data.sde.network.SdeLatestJsonDto
import kotlinx.serialization.decodeFromString

data class SdeRemotePackage(
    val meta: SdeReleaseMeta,
    val assetUrls: Map<String, String>,
) {

    fun urlFor(assetName: String): String? = assetUrls[assetName]

    companion object {
        fun fromJson(json: String): SdeRemotePackage =
            PyeriteJson.decodeFromString<SdeLatestJsonDto>(json).toRemotePackage()
    }
}
