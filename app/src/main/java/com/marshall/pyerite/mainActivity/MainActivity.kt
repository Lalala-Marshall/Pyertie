package com.marshall.pyerite.mainActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marshall.pyerite.appNavHost.AppNavHost
import com.marshall.pyerite.ui.theme.PyeriteTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PyeriteTheme {
                AppNavHost()
            }
        }
    }
}