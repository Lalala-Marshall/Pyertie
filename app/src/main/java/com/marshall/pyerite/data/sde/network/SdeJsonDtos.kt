package com.marshall.pyerite.data.sde.network

import com.marshall.pyerite.data.sde.SdeReleaseMeta
import com.marshall.pyerite.data.sde.SdeRemotePackage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SdeLatestJsonDto(
    @SerialName("build_number") val buildNumber: String = "0",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("completion_time") val completionTime: String = "",
    @SerialName("icon_version") val iconVersion: Int? = null,
    @SerialName("icon_sha256") val iconSha256: String? = null,
    @SerialName("sde_sha256") val sdeSha256: String? = null,
    val assets: Map<String, String> = emptyMap(),
    @SerialName("assets_base_url") val assetsBaseUrl: String = "",
) {
    fun toReleaseMeta(): SdeReleaseMeta = SdeReleaseMeta(
        buildNumber = buildNumber,
        releaseDate = releaseDate,
        completionTime = completionTime,
        iconVersion = iconVersion?.takeIf { it >= 0 },
        iconSha256 = iconSha256?.ifBlank { null },
        sdeSha256 = sdeSha256?.ifBlank { null },
    )

    fun toRemotePackage(): SdeRemotePackage {
        val assetUrls = linkedMapOf<String, String>()
        assets.forEach { (key, url) ->
            val trimmed = url.trim()
            if (trimmed.isNotEmpty()) assetUrls[key] = trimmed
        }
        val baseUrl = assetsBaseUrl.trim().removeSuffix("/")
        if (baseUrl.isNotEmpty()) {
            listOf(ASSET_DB_ZH, ASSET_DB_EN, ASSET_ICONS).forEach { name ->
                assetUrls.putIfAbsent(name, "$baseUrl/$name")
            }
        }
        return SdeRemotePackage(
            meta = toReleaseMeta(),
            assetUrls = assetUrls,
        )
    }

    companion object {
        private const val ASSET_DB_ZH = "item_db_zh.sqlite"
        private const val ASSET_DB_EN = "item_db_en.sqlite"
        private const val ASSET_ICONS = "icons.zip"
    }
}

@Serializable
data class GitHubReleaseDto(
    val assets: List<GitHubAssetDto> = emptyList(),
)

@Serializable
data class GitHubAssetDto(
    val name: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = "",
)
