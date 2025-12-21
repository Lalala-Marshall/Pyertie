package com.marshall.pyerite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.theme.PyeriteTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PyeriteTheme {
                MainActivityContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@Preview(showBackground = true)
@Composable
fun MainActivityContentPreview() {
    PyeriteTheme {
        MainActivityContent()
    }
}