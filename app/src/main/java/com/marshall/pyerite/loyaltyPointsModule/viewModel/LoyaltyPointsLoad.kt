package com.marshall.pyerite.loyaltyPointsModule.viewModel

import android.util.Log
import kotlin.coroutines.cancellation.CancellationException

private const val LOYALTY_POINTS_LOG_TAG = "LoyaltyPoints"

/**
 * Like [runCatching], but does not treat coroutine cancellation as a load failure.
 * Loyalty pages call language reload from `LaunchedEffect` on first composition;
 * that used to cancel the `init` load and stamp `loadFailed`.
 */
internal inline fun <T> loyaltyPointsRunCatching(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Throwable) {
        Log.w(LOYALTY_POINTS_LOG_TAG, "load failed", error)
        Result.failure(error)
    }
}
