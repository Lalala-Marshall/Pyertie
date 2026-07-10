package com.marshall.pyerite.databaseHierarchyModule.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Composable
fun rememberSearchRowHeight(): Dp {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val verticalPadding = dimensionResource(R.dimen.search_bar_vertical_padding)
    return barHeight + verticalPadding * 2
}

/** Idle search row in the list sticky header; tap to activate search. */
@Composable
fun DatabaseSearchIdleBar(
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
    transparentContainer: Boolean = false,
) {
    SearchRowContainer(
        modifier = modifier,
        transparentBackground = transparentContainer,
    ) {
        DatabaseSearchIdleField(
            onActivate = onActivate,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.search_bar_height)),
        )
    }
}

/** Active search row for the pinned top header; tap scrim elsewhere to dismiss when query is blank. */
@Composable
fun DatabaseSearchActiveBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    transparentContainer: Boolean = true,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        SearchRowContainer(transparentBackground = transparentContainer) {
            DatabaseSearchActiveField(
                query = query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
                focusRequester = focusRequester,
                rowMaxWidth = maxWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.search_bar_height)),
            )
        }
    }
}

/** Active search field in overlay; no row background so scrim shows in side padding. */
@Composable
fun DatabaseSearchActiveField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    rowMaxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val horizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.search_bar_vertical_padding)

    DatabaseSearchTextField(
        query = query,
        onQueryChange = onQueryChange,
        onClearQuery = onClearQuery,
        focusRequester = focusRequester,
        modifier = modifier
            .padding(top = verticalPadding)
            .width(rowMaxWidth - horizontalPadding * 2)
            .height(barHeight),
    )
}

@Composable
private fun SearchRowContainer(
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false,
    content: @Composable () -> Unit,
) {
    val horizontalPadding = dimensionResource(R.dimen.detail_card_horizontal_padding)
    val verticalPadding = dimensionResource(R.dimen.search_bar_vertical_padding)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (transparentBackground) {
                    Modifier
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background)
                },
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    ) {
        content()
    }
}

@Composable
private fun DatabaseSearchIdleField(
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barHeight = dimensionResource(R.dimen.search_bar_height)
    val fieldBackground = colorResource(R.color.second_background)
    val hintColor = colorResource(R.color.hint_text)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(barHeight / 2))
            .background(fieldBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onActivate,
            )
            .semantics { role = Role.Button },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp),
        )
        Text(
            text = stringResource(R.string.search),
            color = hintColor,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun DatabaseSearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val corner = dimensionResource(R.dimen.detail_card_corner_radius)
    val fieldBackground = colorResource(R.color.search_field_background)
    val textColor = colorResource(R.color.text_primary)
    val hintColor = colorResource(R.color.hint_text)

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(fieldBackground)
            .focusRequester(focusRequester),
        textStyle = TextStyle(color = textColor, fontSize = 16.sp),
        singleLine = true,
        cursorBrush = SolidColor(textColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = hintColor,
                    modifier = Modifier.size(20.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search),
                            color = hintColor,
                            fontSize = 16.sp,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    SearchClearButton(onClick = onClearQuery)
                }
            }
        },
    )
}

@Composable
private fun SearchClearButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.search_clear_button_background)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.search_clear),
                tint = colorResource(R.color.search_clear_icon),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
