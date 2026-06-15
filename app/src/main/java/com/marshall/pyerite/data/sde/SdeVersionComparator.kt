package com.marshall.pyerite.data.sde

object SdeVersionComparator {

    fun needsUpgrade(candidate: SdeReleaseMeta, installed: SdeReleaseMeta?): Boolean {
        if (installed == null) return true

        val candidateKey = candidate.versionKey()
        val installedKey = installed.versionKey()
        if (candidateKey > installedKey) return true
        if (candidateKey < installedKey) return false

        if (candidate.sdeSha256 != null && candidate.sdeSha256 != installed.sdeSha256) return true
        if (candidate.iconSha256 != null && candidate.iconSha256 != installed.iconSha256) return true
        return false
    }
}
