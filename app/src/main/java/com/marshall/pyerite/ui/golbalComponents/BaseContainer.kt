package com.marshall.pyerite.ui.golbalComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marshall.pyerite.R

@Composable
fun BaseContainer(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.systemBarsPadding()) {
        title?.let {
            Text(
                text = it,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.padding(bottom = 4.dp).padding(start = 24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    Modifier.padding(horizontal = 16.dp)
                )
                .background(
                    color = colorResource(R.color.second_background), // second_background
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                content()
            }
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