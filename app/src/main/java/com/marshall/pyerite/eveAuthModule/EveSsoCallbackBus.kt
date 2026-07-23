package com.marshall.pyerite.eveAuthModule

import android.net.Uri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Delivers EVE SSO callback URIs from MainActivity into the auth layer. */
class EveSsoCallbackBus {
    private val _callbacks = MutableSharedFlow<Uri>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val callbacks: SharedFlow<Uri> = _callbacks.asSharedFlow()

    fun offer(uri: Uri) {
        _callbacks.tryEmit(uri)
    }
}
