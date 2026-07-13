package com.marshall.pyerite.data.sde

import android.content.Context
import org.json.JSONObject

data class SdeVersionKey(
    val buildNumber: Int,
    val releaseDate: String,
    val completionTime: String,
) : Comparable<SdeVersionKey> {

    override fun compareTo(other: SdeVersionKey): Int {
        if (buildNumber != other.buildNumber) return buildNumber.compareTo(other.buildNumber)
        if (releaseDate != other.releaseDate) return releaseDate.compareTo(other.releaseDate)
        return completionTime.compareTo(other.completionTime)
    }

}

data class SdeReleaseMeta(
    val buildNumber: String,
    val releaseDate: String,
    val completionTime: String,
    val iconVersion: Int?,
    val iconSha256: String?,
    val sdeSha256: String?,
) {

    fun versionKey(): SdeVersionKey = SdeVersionKey(
        buildNumber = buildNumber.toIntOrNull() ?: 0,
        releaseDate = releaseDate,
        completionTime = completionTime,
    )

    companion object {
        private const val ASSET_LATEST = "latest.txt"

        fun fromJsonObject(json: JSONObject): SdeReleaseMeta = SdeReleaseMeta(
            buildNumber = json.optString("build_number", "0"),
            releaseDate = json.optString("release_date", ""),
            completionTime = json.optString("completion_time", ""),
            iconVersion = json.optInt("icon_version", -1).takeIf { it >= 0 },
            iconSha256 = json.optString("icon_sha256").ifBlank { null },
            sdeSha256 = json.optString("sde_sha256").ifBlank { null },
        )

        fun fromAssets(context: Context): SdeReleaseMeta? {
            for (assetName in listOf(ASSET_LATEST, "latest.json")) {
                val meta = readFromAsset(context, assetName) ?: continue
                return meta
            }
            return null
        }

        private fun readFromAsset(context: Context, assetName: String): SdeReleaseMeta? = try {
            context.assets.open(assetName).bufferedReader().use { reader ->
                fromJsonObject(JSONObject(reader.readText()))
            }
        } catch (_: Exception) {
            null
        }
    }
}
