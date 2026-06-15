package com.marshall.pyerite.ui.golbalComponents

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Composable
fun BaseContainer(
    title: String? = null,
    titleTrailingContent: @Composable (() -> Unit)? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    useSystemBarsPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    val containerModifier = if (useSystemBarsPadding) modifier.systemBarsPadding() else modifier
    Column(modifier = containerModifier) {
        if (title != null || titleTrailingContent != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title?.let {
                    Text(
                        text = it,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colorResource(R.color.text_primary),
                    )
                } ?: Spacer(modifier = Modifier.weight(1f))
                titleTrailingContent?.invoke()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = colorResource(R.color.second_background),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityContent() {
    BaseContainer(
        title = "Container title",
        content = {
            Column {
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
                Text("Container content", color = colorResource(R.color.text_primary))
            }
        },
    )
}