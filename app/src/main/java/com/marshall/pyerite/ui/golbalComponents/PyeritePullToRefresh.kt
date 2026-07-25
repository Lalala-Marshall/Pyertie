package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marshall.pyerite.R

/**
 * Pull-to-refresh shell shared by main page / character list / sheet.
 *
 * Material [PullToRefreshBox] keeps `isRefreshing = false` so content snaps back on
 * release; in-progress / failure feedback belongs in the top bar via
 * [pyeritePullRefreshTopBarAction].
 */
@Composable
fun PyeritePullToRefreshBox(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val pullRefreshState = rememberPullToRefreshState()
    val pullRefreshMaxDistance = PullToRefreshDefaults.IndicatorMaxDistance
    val pullRefreshMaxDistancePx = with(LocalDensity.current) {
        pullRefreshMaxDistance.toPx()
    }
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = onRefresh,
        modifier = modifier,
        state = pullRefreshState,
        indicator = {
            PyeritePullRefreshIndicator(
                state = pullRefreshState,
                maxDistance = pullRefreshMaxDistance,
            )
        },
    ) {
        val contentOffsetY = pullRefreshState.distanceFraction * pullRefreshMaxDistancePx
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = contentOffsetY },
            content = content,
        )
    }
}

/**
 * Top-bar refresh affordance shown while a pull/manual refresh runs or after failure.
 * Returns null when idle and not failed.
 */
@Composable
fun pyeritePullRefreshTopBarAction(
    isRefreshing: Boolean,
    refreshFailed: Boolean,
    onRefresh: () -> Unit,
): PyeriteTopBarActionItem? {
    if (!isRefreshing && !refreshFailed) return null
    return PyeriteTopBarActionItem(
        onClick = {
            if (!isRefreshing) {
                onRefresh()
            }
        },
        icon = Icons.Default.Refresh,
        contentDescription = stringResource(R.string.character_pull_to_refresh),
        iconTint = if (isRefreshing) {
            colorResource(R.color.hyperlink_text)
        } else {
            colorResource(R.color.character_delete)
        },
        enabled = !isRefreshing,
        spinning = isRefreshing,
    )
}

@Composable
private fun BoxScope.PyeritePullRefreshIndicator(
    state: PullToRefreshState,
    maxDistance: Dp,
) {
    val pullProgress = state.distanceFraction.coerceIn(
        PyeritePullRefreshConfig.PROGRESS_MIN,
        PyeritePullRefreshConfig.PROGRESS_MAX,
    )

    PullToRefreshDefaults.IndicatorBox(
        state = state,
        isRefreshing = false,
        modifier = Modifier.align(Alignment.TopCenter),
        maxDistance = maxDistance,
        containerColor = Color.Transparent,
        elevation = PyeritePullRefreshConfig.INDICATOR_ELEVATION,
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = stringResource(R.string.character_pull_to_refresh),
            tint = colorResource(R.color.hyperlink_text),
            modifier = Modifier
                .size(dimensionResource(R.dimen.character_pull_refresh_icon_size))
                .graphicsLayer {
                    rotationZ = pullProgress * PyeritePullRefreshConfig.PULL_ROTATION_DEGREES
                    alpha = pullProgress
                },
        )
    }
}

private object PyeritePullRefreshConfig {
    const val PROGRESS_MIN = 0f
    const val PROGRESS_MAX = 1f
    const val PULL_ROTATION_DEGREES = 180f
    val INDICATOR_ELEVATION = 0.dp
}
