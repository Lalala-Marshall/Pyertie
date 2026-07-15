package com.marshall.pyerite.data.sde

object SdeRemoteConfig {
    const val GITHUB_REPO = "Lalala-Marshall/Pyertie"
    const val LATEST_JSON_ASSET = "latest.json"
    const val LATEST_JSON_URL = "https://github.com/$GITHUB_REPO/releases/latest/download/$LATEST_JSON_ASSET"
    const val GITHUB_API_LATEST_RELEASE_URL =
        "https://api.github.com/repos/$GITHUB_REPO/releases/latest"
    /** Dummy base URL; calls use absolute retrofit `@Url` values. */
    const val RETROFIT_BASE_URL = "https://github.com/"
}
