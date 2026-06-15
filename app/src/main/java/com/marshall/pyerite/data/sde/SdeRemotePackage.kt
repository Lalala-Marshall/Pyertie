package com.marshall.pyerite.data.sde

import org.json.JSONObject

data class SdeRemotePackage(
    val meta: SdeReleaseMeta,
    val assetUrls: Map<String, String>,
) {

    fun urlFor(assetName: String): String? = assetUrls[assetName]

    companion object {
        private const val ASSET_DB_ZH = "item_db_zh.sqlite"
        private const val ASSET_DB_EN = "item_db_en.sqlite"
        private const val ASSET_ICONS = "icons.zip"

        fun fromJsonObject(json: JSONObject): SdeRemotePackage {
            val assets = linkedMapOf<String, String>()
            val assetsObject = json.optJSONObject("assets")
            if (assetsObject != null) {
                assetsObject.keys().forEach { key ->
                    val url = assetsObject.optString(key).trim()
                    if (url.isNotEmpty()) assets[key] = url
                }
            }

            val baseUrl = json.optString("assets_base_url").trim().removeSuffix("/")
            if (baseUrl.isNotEmpty()) {
                listOf(ASSET_DB_ZH, ASSET_DB_EN, ASSET_ICONS).forEach { name ->
                    assets.putIfAbsent(name, "$baseUrl/$name")
                }
            }

            return SdeRemotePackage(
                meta = SdeReleaseMeta.fromJsonObject(json),
                assetUrls = assets,
            )
        }
    }
}
