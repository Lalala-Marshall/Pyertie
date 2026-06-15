package com.marshall.pyerite.mainPageModule.navHost

sealed class MainRoute(val route: String) {
    object Root : MainRoute("main")
}