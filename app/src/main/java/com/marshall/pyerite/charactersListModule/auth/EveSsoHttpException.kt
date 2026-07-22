package com.marshall.pyerite.charactersListModule.auth

import java.io.IOException

/** Typed SSO token/revoke HTTP failure (avoids parsing error message strings). */
internal class EveSsoHttpException(
    val statusCode: Int,
    message: String,
    cause: Throwable? = null,
    val errorBody: String = "",
) : IOException(message, cause) {

    val isPermanentRefreshFailure: Boolean
        get() {
            if (statusCode in EveSsoConfig.permanentRefreshHttpStatuses) {
                return true
            }
            val body = errorBody.ifBlank { message.orEmpty() }
            return body.contains(EveSsoConfig.OAuth.ERROR_INVALID_GRANT, ignoreCase = true) ||
                body.contains(EveSsoConfig.OAuth.ERROR_INVALID_TOKEN, ignoreCase = true) ||
                body.contains(EveSsoConfig.OAuth.ERROR_UNAUTHORIZED_CLIENT, ignoreCase = true)
        }
}
