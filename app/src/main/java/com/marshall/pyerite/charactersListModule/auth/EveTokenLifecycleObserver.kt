package com.marshall.pyerite.charactersListModule.auth

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Refreshes near-expiry SSO tokens when the app returns to the foreground. */
internal class EveTokenLifecycleObserver(
    private val authRepository: EveSsoAuthRepository,
) : DefaultLifecycleObserver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStart(owner: LifecycleOwner) {
        scope.launch {
            authRepository.refreshStoredSessionsOnForeground()
        }
    }
}
