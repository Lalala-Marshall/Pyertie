package com.marshall.pyerite.mainPageModule.navHostMainPage

sealed class MainRoute(val route: String) {
    object Root : MainRoute("main")
}