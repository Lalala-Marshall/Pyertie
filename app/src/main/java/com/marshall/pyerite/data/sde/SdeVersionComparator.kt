package com.marshall.pyerite.data.sde

object SdeVersionComparator {

    fun needsUpgrade(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        return needsDatabaseUpgrade(candidate, installed) || needsIconsUpgrade(candidate, installed)
    }

    fun needsDatabaseUpgrade(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return true

        val candidateKey = candidate.versionKey()
        val installedKey = installed.versionKey()
        if (candidateKey > installedKey) return true
        if (candidateKey < installedKey) return false

        return candidate.sdeSha256 != null && candidate.sdeSha256 != installed.sdeSha256
    }

    fun needsIconsUpgrade(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return true

        if (iconsVersionAligned(candidate, installed)) {
            SdeUpdateLog.d(
                "VersionComparator",
                "icons version aligned at ${candidate.iconVersion}, " +
                    "installedSha=${installed.iconSha256} remoteSha=${candidate.iconSha256}",
            )
            if (iconSha256Mismatch(candidate, installed)) {
                SdeUpdateLog.d("VersionComparator", "icon_sha256 mismatch, icons still need upgrade")
                return true
            }
            SdeUpdateLog.d("VersionComparator", "icons version and icon_sha256 aligned, skip icons")
            return false
        }

        if (iconSha256Mismatch(candidate, installed)) return true

        val remoteIconVersion = candidate.iconVersion
        val installedIconVersion = installed.iconVersion
        if (remoteIconVersion != null && installedIconVersion != null) {
            return remoteIconVersion != installedIconVersion
        }

        return false
    }

    fun iconsVersionAligned(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return false
        val remoteIconVersion = candidate.iconVersion ?: return false
        val installedIconVersion = installed.iconVersion ?: return false
        return remoteIconVersion == installedIconVersion
    }

    fun iconsSha256Aligned(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return false
        return !iconSha256Mismatch(candidate, installed)
    }

    private fun iconSha256Mismatch(candidate: SdeReleaseMeta, installed: SdeReleaseMeta): Boolean {
        return candidate.iconSha256 != null && candidate.iconSha256 != installed.iconSha256
    }
}
