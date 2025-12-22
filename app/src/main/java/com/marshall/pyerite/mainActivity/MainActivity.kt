package com.marshall.pyerite.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.marshall.pyerite.R
import com.marshall.pyerite.ui.golbalComponents.BaseContainer
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumn
import com.marshall.pyerite.ui.golbalComponents.BaseLazyColumnItemModel
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
        title = stringResource(R.string.data),
        content = {
            val items = listOf(
                BaseLazyColumnItemModel(
                    iconRes = R.drawable.ic_database,
                    itemName = stringResource(R.string.database)
                ),
            )
            BaseLazyColumn(items = items)
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